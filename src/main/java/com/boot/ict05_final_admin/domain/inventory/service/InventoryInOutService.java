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
 * 본사 재고 입/출고 관련 비즈니스 로직을 처리하는 서비스 클래스
 *
 * <p>입고 등록 / 출고 시 본사 재고 수량 갱신을 담당한다.</p>
 *
 * @author ICT
 * @since 2025.10
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InventoryInOutService {

    private final InventoryInRepository inventoryInRepository;
    private final InventoryOutRepository inventoryOutRepository;
    private final InventoryAdjustmentRepository inventoryAdjustmentRepository;
    private final InventoryRepository inventoryRepository;
    private final MaterialRepository materialRepository;
    private final StoreRepository storeRepository;

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
    public Long insertInventoryIn(InventoryInWriteDTO dto) {
        Material material = materialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new IllegalArgumentException("해당 재료를 찾을 수 없습니다."));
        HqInventory inventory = inventoryRepository.findByMaterial(material)
                .orElseThrow(() -> new IllegalArgumentException("해당 재료의 본사 재고 정보가 없습니다."));

        // 1. 재고 증가 및 즉시 반영
        inventoryRepository.addQuantity(material.getId(), dto.getQuantity());
        inventoryRepository.flush();

        // 2. 재고량 재조회 (정확한 최신 수량)
        BigDecimal updatedQty = inventoryRepository.findByMaterial(material)
                .map(HqInventory::getQuantity)
                .orElseThrow(() -> new IllegalStateException("재고 재조회 실패"));

        // 3. 입고 로그 기록
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
        inventory.setStatus(InventoryStatus.calculate(inventory.getQuantity(), material.getOptimalQuantity()));
        inventoryRepository.save(inventory);
        log.info("[입고등록] materialId={}, 입고={}, 재고={}, 상태={}", material.getId(), dto.getQuantity(), updatedQty, inventory.getStatus());

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
    public Long insertInventoryOut(Long materialId, BigDecimal quantity, Long storeId, String memo) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("해당 재료를 찾을 수 없습니다."));
        HqInventory inventory = inventoryRepository.findByMaterial(material)
                .orElseThrow(() -> new IllegalArgumentException("해당 재료의 본사 재고 정보가 없습니다."));

        if (inventory.getQuantity().compareTo(quantity) < 0) {
            throw new IllegalArgumentException("출고 수량이 현재 재고보다 많습니다.");
        }

        // 1. 재고 차감 및 즉시 반영
        inventoryRepository.subtractQuantity(material.getId(), quantity);
        inventoryRepository.flush();

        // 2. 재고량 재조회
        BigDecimal updatedQty = inventoryRepository.findByMaterial(material)
                .map(HqInventory::getQuantity)
                .orElseThrow(() -> new IllegalStateException("재고 재조회 실패"));

        // 3. 출고 로그 기록
        Store store = (storeId != null) ? storeRepository.getReferenceById(storeId) : null;
        InventoryOut entity = InventoryOut.builder()
                .material(material)
                .store(store)
                .quantity(quantity)
                .unitPrice(0L)
                .outDate(LocalDateTime.now())
                .memo(memo)
                .stockAfter(updatedQty)
                .build();
        inventoryOutRepository.save(entity);

        // 4. 상태 갱신
        inventory.setStatus(InventoryStatus.calculate(inventory.getQuantity(), material.getOptimalQuantity()));
        inventoryRepository.save(inventory);
        log.info("[출고등록] materialId={}, 출고={}, 재고={}, 상태={}", material.getId(), quantity, updatedQty, inventory.getStatus());


        return entity.getId();
    }

    /**
     * 본사 재고 수량 조정
     *
     * <p>입출고 외의 사유(분실, 파손, 오입력 등)로 재고 수량을 수정할 때 사용.</p>
     */
    @Transactional
    public void adjustInventory(InventoryAdjustDTO dto) {
        HqInventory inventory = inventoryRepository.findById(dto.getInventoryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 재고 정보를 찾을 수 없습니다."));

        BigDecimal before = inventory.getQuantity();
        BigDecimal after = dto.getQuantityAfter();
        BigDecimal diff = after.subtract(before);

        inventory.setQuantity(after);

        InventoryAdjustment adjustment = InventoryAdjustment.builder()
                .inventory(inventory)
                .materialId(dto.getMaterialId())
                .quantityBefore(before)
                .quantityAfter(after)
                .difference(diff)
                .memo(dto.getMemo())
                .reason(dto.getReason())
                .createdAt(LocalDateTime.now())
                .build();

        inventoryAdjustmentRepository.save(adjustment);

        // 상태 재계산 및 반영
        inventory.setStatus(InventoryStatus.calculate(inventory.getQuantity(), inventory.getMaterial().getOptimalQuantity()));
        inventoryRepository.save(inventory);

        log.info("[HQ INVENTORY ADJUST] materialId={}, before={}, after={}, diff={}, reason={}, memo={}",
                dto.getMaterialId(), before, after, diff, dto.getReason(), dto.getMemo());
    }

}