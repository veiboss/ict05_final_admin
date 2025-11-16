package com.boot.ict05_final_admin.domain.inventory.service;


import com.boot.ict05_final_admin.domain.inventory.dto.InventoryAdjustDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.*;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryAdjustmentRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.UnitPriceRepository;
import static com.boot.ict05_final_admin.domain.inventory.utility.InventoryLogIdUtil.unwrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;



/**
 * 재고 조정 서비스 (InventoryAdjustmentService)
 *
 * <p>대상은 <b>재고ID</b> 기준이다. Δ(증감치)는 {@code difference}를 사용한다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryAdjustmentService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final InventoryRepository inventoryRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final InventoryAdjustmentRepository inventoryAdjustmentRepository;
    private final UnitPriceRepository unitPriceRepository;

    /**
     * 기준 시점(at)에 유효한 최신 매입단가를 조회한다.
     */
    private BigDecimal getLatestPurchasePrice(Long materialId, LocalDateTime at) {
        return unitPriceRepository.findLatestPurchasePrice(materialId, at)
                .map(UnitPrice::getPurchasePrice)
                .orElse(ZERO);
    }

    /**
     * 본사 재고 수량을 직접 조정한다.
     *
     * @param dto 재고 수량 조정 요청 DTO
     */
    @Transactional
    public void adjustInventory(InventoryAdjustDTO dto) {

        log.info("[ADJUST_REQ] dto={}", dto);

        // 1) 대상 재고 조회
        Inventory inventory = inventoryRepository.findById(dto.getInventoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 재고 정보를 찾을 수 없습니다. id=" + dto.getInventoryId()));

        Material material = inventory.getMaterial();

        // 2) before / after / diff 계산
        BigDecimal before = inventory.getQuantity() != null ? inventory.getQuantity() : ZERO;
        BigDecimal after  = dto.getQuantityAfter();
        if (after == null) {
            throw new IllegalArgumentException("조정 후 수량(quantityAfter)은 필수입니다.");
        }

        BigDecimal diff = after.subtract(before);

        // DTO에도 세팅(필요 시 응답/로그에서 사용)
        dto.setQuantityBefore(before);
        dto.setDifference(diff);

        // 수량이 0 미만이 되는지 확인
        if (after.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("조정 후 수량이 0 미만일 수 없습니다. quantityAfter=" + after);
        }

        // 3) 재고 수량 반영 + 상태/업데이트 일시 동기화
        inventory.setQuantity(after);
        inventory.touchAfterQuantityChange();   // InventoryBase 공통 메서드 사용

        inventoryRepository.saveAndFlush(inventory);

        log.info("[ADJUST_DONE] invId={}, before={}, after={}, diff={}",
                inventory.getId(), before, after, diff);

        // 4) 증감분에 따른 배치 처리
        if (diff.compareTo(ZERO) > 0) {
            // 수량 증가 → ADJ 로트 추가
            String lotNo = String.format("%s-%s-%s",
                    material.getCode(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    "ADJ");

            LocalDateTime now = LocalDateTime.now();
            BigDecimal latestPrice = getLatestPurchasePrice(material.getId(), now);

            InventoryBatch batch = InventoryBatch.builder()
                    .material(material)
                    .lotNo(lotNo)
                    .quantity(diff)
                    .unitPrice(latestPrice)
                    .receivedDate(now)
                    .createdAt(now)
                    .build();

            inventoryBatchRepository.saveAndFlush(batch);

        } else if (diff.compareTo(ZERO) < 0) {
            // 수량 감소 → FIFO 차감
            BigDecimal remaining = diff.abs();

            // 커스텀 레포지토리 구현에 맞춰 FIFO 후보 조회
            List<InventoryBatch> batches =
                    inventoryBatchRepository.findFifoCandidates(material.getId());

            for (InventoryBatch b : batches) {
                if (remaining.compareTo(ZERO) <= 0) break;

                BigDecimal avail = b.getQuantity();
                if (avail == null || avail.compareTo(ZERO) <= 0) continue;

                BigDecimal used = remaining.min(avail);

                b.setQuantity(avail.subtract(used));
                inventoryBatchRepository.save(b);

                remaining = remaining.subtract(used);
                inventoryBatchRepository.save(b); // 필요하면 여기서도 flush 가능

                remaining = remaining.subtract(used);
            }
            // remaining > 0 인 경우: 음수 재고 허용 여부에 따라 정책 추가 가능
        }

        // 5) 조정 로그 기록 (createdAt 명시, createdAt 기준 조회와 일치)
        LocalDateTime now = LocalDateTime.now();
        BigDecimal latestPriceForLog = getLatestPurchasePrice(material.getId(), now);

        InventoryAdjustment adj = InventoryAdjustment.builder()
                .inventory(inventory)
                .quantityBefore(before)
                .quantityAfter(after)
                .difference(diff)
                .unitPrice(latestPriceForLog)
                .memo(dto.getMemo())
                .reason(dto.getReason())
                .createdAt(now)
                .build();

        inventoryAdjustmentRepository.save(adj);

        // 6) 로그
        log.info(
                "[INVENTORY ADJUST] material={}, diff={}, before={}, after={}, reason={}",
                material.getCode(), diff, before, after, dto.getReason()
        );
    }

    /**
     * 재고 조정 상세 조회
     *
     * @param logId inventory_adjustment.adjustment_id (로그 ID)
     * @return 조정 상세 DTO
     */
    @Transactional(readOnly = true)
    public InventoryAdjustDTO getAdjustDetail(Long logId) {
        long pk = unwrap(logId); // 2000000001 → 1 이런 식으로 언랩

        InventoryAdjustment adj = inventoryAdjustmentRepository.findById(pk)
                .orElseThrow(() ->
                        new IllegalArgumentException("재고 조정 이력을 찾을 수 없습니다. id=" + logId));

        return InventoryAdjustDTO.builder()
                .inventoryId(adj.getInventory().getId())
                .material(adj.getInventory().getMaterial().getId())
                .quantityBefore(adj.getQuantityBefore())
                .quantityAfter(adj.getQuantityAfter())
                .difference(adj.getDifference())
                .reason(adj.getReason())
                .memo(adj.getMemo())
                .build();
    }
}
