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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 본사 재고 입/출고 및 수량 조정 처리 서비스 클래스
 *
 * <p>입출고 시 본사 재고 수량을 자동 갱신하고,
 * 로트(InventoryBatch)와 단가(UnitPrice) 정보를 함께 관리한다.</p>
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

    // 로트 및 단가 관련
    private final InventoryBatchRepository inventoryBatchRepository;
    private final UnitPriceRepository unitPriceRepository;

    private static final BigDecimal ZERO = BigDecimal.ZERO;

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
     * <p>입고 시 로트(InventoryBatch) 생성 및 단가(UnitPrice) 이력 갱신</p>
     *
     * @param dto 입고 등록 정보 (재료 ID, 입고 수량, 단가, 입고일, 비고)
     * @return 생성된 입고 이력 ID
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
                            .optimalQuantity(material.getOptimalQuantity() != null ? material.getOptimalQuantity() : ZERO)
                            .status(InventoryStatus.SUFFICIENT)
                            .updateDate(LocalDateTime.now())
                            .build();
                    inventoryRepository.save(newInv);
                    return newInv;
                });

        // 1. 단가 이력 기록
        UnitPrice unitPrice = UnitPrice.builder()
                .material(material)
                .purchasePrice(dto.getUnitPrice())
                .createdAt(LocalDateTime.now())
                .validFrom(LocalDateTime.now())
                .build();
        unitPriceRepository.save(unitPrice);

        // 2. 로트번호 생성
        Long todayCount = inventoryBatchRepository.countTodayByMaterial(material.getId());
        long seq = (todayCount == null ? 0 : todayCount) + 1;
        String lotNo = String.format("%s-%s-%04d",
                material.getCode(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                seq);

        // 3. 배치 생성
        InventoryBatch batch = InventoryBatch.builder()
                .material(material)
                .lotNo(lotNo)
                .receivedDate(dto.getInDate() != null ? dto.getInDate() : LocalDateTime.now()) // 사용자가 지정한 입고일과 동일
                .expirationDate(LocalDate.now().plusMonths(6)) // 임시 유통기한
                .quantity(dto.getQuantity())
                .receivedQuantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .createdAt(LocalDateTime.now())
                .build();
        inventoryBatchRepository.save(batch);

        // 4. 본사 재고 수량 갱신
        BigDecimal newQty = inventory.getQuantity().add(dto.getQuantity());
        inventory.setQuantity(newQty);
        inventory.setUpdateDate(LocalDateTime.now());
        inventory.updateStatus();
        inventoryRepository.save(inventory);

        // 5. 로그 저장
        InventoryIn entity = InventoryIn.builder()
                .material(material)
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .inDate(dto.getInDate() != null ? dto.getInDate() : LocalDateTime.now())
                .memo(dto.getMemo())
                .stockAfter(newQty)
                .lotNo(lotNo)
                .build();
        inventoryInRepository.save(entity);

        updateInventoryStatus(inventory, material);
        log.info("[INVENTORY IN] material={}, lot={}, qty={}, price={}", material.getCode(), lotNo, dto.getQuantity(), dto.getUnitPrice());

        return entity.getId();
    }

    /**
     * 본사 출고 등록
     *
     * <p>FIFO 순으로 로트 차감 및 출고 단가 적용</p>
     */
    @Transactional
    public Long insertInventoryOut(Long materialId, BigDecimal quantity, Long storeId, String memo) {
        Material material = getMaterialOrThrow(materialId);
        HqInventory inventory = getInventoryOrThrow(material);

        if (inventory.getQuantity().compareTo(quantity) < 0)
            throw new IllegalArgumentException("출고 수량이 현재 재고보다 많습니다.");

        BigDecimal remaining = quantity;
        BigDecimal lastUsedPrice = BigDecimal.ZERO;

        // 1. 차감 가능한 로트 조회 (입고순)
        List<InventoryBatch> batches = inventoryBatchRepository.findAvailableBatches(material.getId());
        if (batches.isEmpty()) {
            throw new IllegalStateException("차감 가능한 로트가 없습니다.");
        }

        // 2. FIFO 순서대로 차감
        for (InventoryBatch batch : batches) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal available = batch.getQuantity();
            if (available.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal used = remaining.min(available);
            batch.subtractQuantity(used);

            inventoryBatchRepository.save(batch); // 변경 감지용
            lastUsedPrice = batch.getUnitPrice();
            remaining = remaining.subtract(used);

            log.info("[BATCH OUT] lot={}, used={}, remain={}, price={}",
                    batch.getLotNo(), used, batch.getQuantity(), batch.getUnitPrice());
        }

        // ✅ 확실히 DB 반영
        inventoryBatchRepository.flush();


        // 3. 남은 수량이 있다면 로트 부족 예외
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("로트 재고가 부족합니다. 남은 미차감 수량=" + remaining);
        }

        // 4. HQ 재고 차감
        inventoryRepository.subtractQuantity(material.getId(), quantity);
        inventoryRepository.flush();

        BigDecimal updatedQty = inventoryRepository.findByMaterial(material)
                .map(HqInventory::getQuantity)
                .orElse(BigDecimal.ZERO);

        // 5. 출고 로그 저장
        Store store = (storeId != null) ? storeRepository.getReferenceById(storeId) : null;
        InventoryOut out = InventoryOut.builder()
                .material(material)
                .store(store)
                .quantity(quantity)
                .unitPrice(lastUsedPrice)
                .memo(memo)
                .outDate(LocalDateTime.now())
                .stockAfter(updatedQty)
                .build();
        inventoryOutRepository.save(out);

        // 6. 상태 갱신
        updateInventoryStatus(inventory, material);

        log.info("[INVENTORY OUT] material={}, totalOut={}, afterQty={}, lastPrice={}",
                material.getCode(), quantity, updatedQty, lastUsedPrice);

        return out.getId();
    }

    /**
     * 본사 재고 수량 조정
     *
     * <p>로트와 재고 동기화 (부족 시 새 로트, 초과 시 FIFO 차감)</p>
     */
    @Transactional
    public void adjustInventory(InventoryAdjustDTO dto) {
        HqInventory inventory = inventoryRepository.findById(dto.getInventoryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 재고 정보를 찾을 수 없습니다."));
        Material material = inventory.getMaterial();

        BigDecimal before = inventory.getQuantity();
        BigDecimal after = dto.getQuantityAfter();
        BigDecimal diff = after.subtract(before);

        inventory.setQuantity(after);

        if (diff.compareTo(ZERO) > 0) {
            // 부족 → 새 로트 추가
            String lotNo = String.format("%s-%s-%s",
                    material.getCode(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    "ADJ");
            InventoryBatch batch = InventoryBatch.builder()
                    .material(material)
                    .lotNo(lotNo)
                    .quantity(diff)
                    .unitPrice(unitPriceRepository.findLatestPrice(material.getId()))
                    .createdAt(LocalDateTime.now())
                    .build();
            inventoryBatchRepository.save(batch);
        } else if (diff.compareTo(ZERO) < 0) {
            // 초과 → FIFO 차감
            BigDecimal remaining = diff.abs();
            List<InventoryBatch> batches = inventoryBatchRepository.findAvailableBatches(material.getId());
            for (InventoryBatch b : batches) {
                if (remaining.compareTo(ZERO) <= 0) break;
                BigDecimal avail = b.getQuantity();
                BigDecimal used = remaining.min(avail);
                b.setQuantity(avail.subtract(used));
                inventoryBatchRepository.save(b);
                remaining = remaining.subtract(used);
            }
        }

        InventoryAdjustment adj = InventoryAdjustment.builder()
                .inventory(inventory)
                .quantityBefore(before)
                .quantityAfter(after)
                .difference(diff)
                .unitPrice(unitPriceRepository.findLatestPrice(material.getId()))
                .memo(dto.getMemo())
                .reason(dto.getReason())
                .build();
        inventoryAdjustmentRepository.save(adj);

        updateInventoryStatus(inventory, material);
        log.info("[INVENTORY ADJUST] material={}, diff={}, after={}, reason={}", material.getCode(), diff, after, dto.getReason());
    }
}