package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryAdjustDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventoryInWriteDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.*;
import com.boot.ict05_final_admin.domain.inventory.repository.*;
import com.boot.ict05_final_admin.domain.store.entity.Store;
import com.boot.ict05_final_admin.domain.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 본사 재고 입/출고 및 수량 조정 처리 서비스 클래스
 *
 * <p>입출고 시 본사 재고 수량을 자동 갱신하고, 조정 시 상태를 반영한다.</p>
 *
 * @author ICT
 * @since 2025.10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryInOutService {

    private final InventoryInRepository inventoryInRepository;
    private final InventoryOutRepository inventoryOutRepository;
    private final InventoryAdjustmentRepository inventoryAdjustmentRepository;
    private final InventoryRepository inventoryRepository;
    private final MaterialRepository materialRepository;
    private final StoreRepository storeRepository;

    private static final long DEFAULT_UNIT_PRICE = 0L;
    
    // 공통 헬퍼
    private Material getMaterialOrThrow(Long materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("해당 재료를 찾을 수 없습니다."));
    }

    private HqInventory getInventoryOrThrow(Material material) {
        return inventoryRepository.findByMaterial(material)
                .orElseThrow(() -> new IllegalArgumentException("해당 재료의 본사 재고 정보가 없습니다."));
    }

    private void updateInventoryStatus(HqInventory inventory, Material material) {
        inventory.setStatus(
                InventoryStatus.calculate(inventory.getQuantity(), material.getOptimalQuantity())
        );
        inventoryRepository.save(inventory);
    }

    /**
     * 본사 입고 등록
     *
     * <p>선택된 재료(Material)에 대한 입고 정보를 등록하고,
     * 해당 재료의 본사 재고 수량을 증가시킨다.</p>
     *
     * @param dto 입고 등록 정보 (재료 ID, 입고 수량, 단가, 입고일, 비고)
     * @return 생성된 입고 이력(InventoryIn)의 ID
     * @throws IllegalArgumentException 재료 또는 재고가 존재하지 않을 경우 발생
     */
    @Transactional
    public Long insertInventoryIn(InventoryInWriteDTO dto) {
        Material material = getMaterialOrThrow(dto.getMaterialId());

        // 0. 본사 재고 조회 또는 신규 생성
        HqInventory inventory = inventoryRepository.findByMaterial(material)
                .orElseGet(() -> {
                    HqInventory newInv = HqInventory.builder()
                            .material(material)
                            .quantity(BigDecimal.ZERO)
                            .optimalQuantity(
                                    material.getOptimalQuantity() != null
                                            ? material.getOptimalQuantity()
                                            : BigDecimal.ZERO)
                            .status(InventoryStatus.SUFFICIENT) // ✅ 초기 상태
                            .updateDate(LocalDateTime.now())
                            .build();
                    inventoryRepository.save(newInv);
                    log.info("[INVENTORY INIT] created new HQ inventory for materialId={}", material.getId());
                    return newInv;
                });

        // 1. 수량 증가
        BigDecimal newQty = inventory.getQuantity().add(dto.getQuantity());
        inventory.setQuantity(newQty);
        inventory.setUpdateDate(LocalDateTime.now());
        inventory.updateStatus();
        inventoryRepository.save(inventory);
        inventoryRepository.flush();

        // 2. 재조회
        BigDecimal updatedQty = inventoryRepository.findByMaterial(material)
                .map(HqInventory::getQuantity)
                .orElseThrow(() -> new IllegalStateException("재고 재조회 실패"));

        // 3. 로그 저장
        InventoryIn entity = InventoryIn.builder()
                .material(material)
                .store(null)
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .sellingPrice(dto.getSellingPrice())
                .inDate(dto.getInDate() != null ? dto.getInDate() : LocalDateTime.now())
                .memo(dto.getMemo())
                .stockAfter(updatedQty)
                .build();
        inventoryInRepository.save(entity);

        // 4. 상태 갱신
        updateInventoryStatus(inventory, material);

        log.info("[INVENTORY IN] materialId={}, qty={}, afterQty={}, status={}",
                material.getId(), dto.getQuantity(), updatedQty, inventory.getStatus());

        return entity.getId();
    }

    /**
     * 본사 출고 등록
     *
     * <p>가맹점의 주문 요청 또는 내부 출고 사유에 따라,
     * 선택된 재료(Material)의 본사 재고를 차감하고 출고 이력을 기록한다.</p>
     *
     * @param materialId 출고할 재료 ID
     * @param quantity   출고 수량
     * @param storeId    출고 대상 가맹점 ID (없을 경우 null)
     * @param memo       비고
     * @return 생성된 출고 이력(InventoryOut)의 ID
     * @throws IllegalArgumentException 재료 또는 재고가 존재하지 않을 경우 발생
     */
    @Transactional
    public Long insertInventoryOut(Long materialId, BigDecimal quantity, Long storeId, String memo) {
        Material material = getMaterialOrThrow(materialId);
        HqInventory inventory = getInventoryOrThrow(material);

        if (inventory.getQuantity().compareTo(quantity) < 0) {
            throw new IllegalArgumentException("출고 수량이 현재 재고보다 많습니다.");
        }

        // 1. 수량 차감
        inventoryRepository.subtractQuantity(material.getId(), quantity);
        inventoryRepository.flush();

        // 2. 재조회
        BigDecimal updatedQty = inventoryRepository.findByMaterial(material)
                .map(HqInventory::getQuantity)
                .orElseThrow(() -> new IllegalStateException("재고 재조회 실패"));

        // 3. 로그 저장
        Store store = (storeId != null) ? storeRepository.getReferenceById(storeId) : null;
        InventoryOut entity = InventoryOut.builder()
                .material(material)
                .store(store)
                .quantity(quantity)
                .unitPrice(DEFAULT_UNIT_PRICE)
                .outDate(LocalDateTime.now())
                .memo(memo)
                .stockAfter(updatedQty)
                .build();
        inventoryOutRepository.save(entity);

        // 4. 상태 갱신
        updateInventoryStatus(inventory, material);

        log.info("[INVENTORY OUT] materialId={}, qty={}, afterQty={}, status={}",
                material.getId(), quantity, updatedQty, inventory.getStatus());

        return entity.getId();
    }

    /**
     * 본사 재고 수량 조정
     *
     * <p>입출고 외의 사유(분실, 파손, 오입력 등)로 재고 수량을 수정할 때 사용.</p>
     *
     * @param dto 조정 요청 정보 (본사 재고 ID, 재료 ID, 조정 후 수량, 비고, 사유)
     * @throws IllegalArgumentException 재고 또는 재료를 찾을 수 없을 경우 발생
     */
    @Transactional
    public void adjustInventory(InventoryAdjustDTO dto) {
        HqInventory inventory = inventoryRepository.findById(dto.getInventoryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 재고 정보를 찾을 수 없습니다."));

        BigDecimal before = inventory.getQuantity();
        BigDecimal after = dto.getQuantityAfter();
        BigDecimal diff = after.subtract(before);

        // 1. 수량 업데이트
        inventory.setQuantity(after);

        // 2. 로그 기록
        InventoryAdjustment adjustment = InventoryAdjustment.builder()
                .inventory(inventory)
                .quantityBefore(before)
                .quantityAfter(after)
                .difference(diff)
                .unitPrice(DEFAULT_UNIT_PRICE)
                .memo(dto.getMemo())
                .reason(dto.getReason())
                .build();
        inventoryAdjustmentRepository.save(adjustment);

        // 3. 상태 갱신
        updateInventoryStatus(inventory, inventory.getMaterial());

        // 4. 로그 출력
        log.info("[INVENTORY ADJUST] materialId={}, before={}, after={}, diff={}, reason={}, memo={}",
                inventory.getMaterial().getId(), before, after, diff, dto.getReason(), dto.getMemo());
    }

}