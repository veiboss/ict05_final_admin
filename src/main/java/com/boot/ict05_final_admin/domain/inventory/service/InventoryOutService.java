package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.OutConfirmRequest;
import com.boot.ict05_final_admin.domain.inventory.dto.OutPreviewItemDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.*;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchQueryRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutLotRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutRepository;
import com.boot.ict05_final_admin.domain.store.entity.Store;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 출고 서비스
 *
 * <p>FIFO 미리보기 슬라이싱과 출고 확정만 담당한다. 조회는 QueryRepository로 위임한다.</p>
 */
@Service
@RequiredArgsConstructor
public class InventoryOutService {

    private final InventoryBatchQueryRepository inventoryBatchQueryRepository;
    private final InventoryOutRepository inventoryOutRepository;
    private final InventoryOutLotRepository outLotRepo;
    private final EntityManager em;
    private final UnitPriceService unitPriceService;
    private final InventoryService inventoryService;

    /**
     * FIFO 미리보기.
     *
     * @param materialId 재료 ID
     * @param qty   총 출고 수량
     * @return 배치 분할 미리보기
     */
    public List<OutPreviewItemDTO> previewFifo(Long materialId, BigDecimal qty) {
        var candidates = inventoryBatchQueryRepository.findAvailableBatchesForFifo(materialId);
        var remain = qty;
        List<OutPreviewItemDTO> plan = new java.util.ArrayList<>();
        for (var c : candidates) {
            if (remain.signum() <= 0) break;
            var take = c.getAvailable().min(remain);
            if (take.signum() > 0) {
                plan.add(OutPreviewItemDTO.builder()
                        .batchId(c.getBatchId())
                        .lotNo(c.getLotNo())
                        .qty(take)
                        .expirationDate(c.getExpirationDate())
                        .build());
                remain = remain.subtract(take);
            }
        }
        return plan;
    }

    /**
     * 출고 단가 결정 규칙:
     *  1) 기준 시점(ts)의 최신 매입단가(UnitPriceType.PURCHASE)
     *  2) 없으면 해당 재료의 가장 최근 출고 단가
     *  3) 그래도 없으면 0
     */
    private BigDecimal resolveOutUnitPrice(Long materialId, LocalDateTime ts) {
        // 1) 단가 이력에서 최신 매입단가
        BigDecimal fromUnitPriceTable = unitPriceService
                .latestPurchasePrice(materialId, ts)
                .map(UnitPrice::getPurchasePrice)
                .orElse(null);

        if (fromUnitPriceTable != null && fromUnitPriceTable.signum() > 0) {
            return fromUnitPriceTable;
        }

        // 2) 마지막 출고 헤더에서 단가
        BigDecimal fromLastOut = inventoryOutRepository
                .findTopByMaterial_IdOrderByOutDateDescIdDesc(materialId)
                .map(InventoryOut::getUnitPrice)
                .orElse(null);

        if (fromLastOut != null && fromLastOut.signum() > 0) {
            return fromLastOut;
        }

        // 3) 그래도 없으면 0
        return BigDecimal.ZERO;
    }

    /**
     * 출고 확정.
     *
     * @param materialId 재료 ID
     * @param storeId    가맹점 ID(선택)
     * @param totalQty   총 출고 수량
     * @param outDate    출고일시
     * @param memo       비고
     * @return 출고 헤더 ID
     */
    @Transactional
    public Long confirmOut(Long materialId,
                           Long storeId,
                           BigDecimal totalQty,
                           LocalDateTime outDate,
                           String memo) {

        if (totalQty == null || totalQty.signum() <= 0) {
            throw new IllegalArgumentException("출고 수량이 0 이하입니다.");
        }

        // 기준 시각
        LocalDateTime ts = (outDate != null) ? outDate : LocalDateTime.now();

        // 1) 현재 HQ 재고 (배치 합계 기준)
        BigDecimal currentStock = inventoryService.hqRemainOfMaterial(materialId);
        if (currentStock == null) currentStock = BigDecimal.ZERO;
        if (currentStock.compareTo(totalQty) < 0) {
            throw new IllegalArgumentException("출고 수량이 현재고를 초과합니다. current=" +
                    currentStock + ", out=" + totalQty);
        }

        // 2) FIFO plan 뽑기
        var plan = previewFifo(materialId, totalQty);

        BigDecimal plannedSum = BigDecimal.ZERO;

        for (var p : plan) {
            plannedSum = plannedSum.add(p.getQty());

            // 배치 차감
            InventoryBatch batch = em.getReference(InventoryBatch.class, p.getBatchId());
            batch.subtractQuantity(p.getQty());

            // out_lot 생성
            InventoryOutLot lot = InventoryOutLot.builder()
                    .out(null) // 아래서 addLotItem 로 셋
                    .batch(batch)
                    .quantity(p.getQty())
                    .build();
            // 헤더 생성 후 addLotItem에서 out 세팅
        }

        if (plannedSum.compareTo(totalQty) != 0) {
            throw new IllegalStateException("FIFO 분할 합계가 요청 수량과 일치하지 않습니다. planned="
                    + plannedSum + ", requested=" + totalQty);
        }

        // 3) 배치 차감까지 끝난 이후 현재고 재계산
        BigDecimal remain = inventoryService.hqRemainOfMaterial(materialId);

        // 4) 단가 (규칙: 최신 매입가 → 마지막 출고단가 → 0)
        BigDecimal unitPrice = resolveOutUnitPrice(materialId, ts);

        // 5) 헤더 생성 (stockAfter 채워서)
        InventoryOut out = InventoryOut.builder()
                .material(em.getReference(Material.class, materialId))
                .store(storeId != null ? em.getReference(Store.class, storeId) : null)
                .outDate(ts)
                .quantity(totalQty)
                .stockAfter(remain)
                .unitPrice(unitPrice)
                .memo(memo)
                .build();

        out = inventoryOutRepository.save(out);

        // 6) LOT 재연결
        for (var p : plan) {
            InventoryOutLot lot = InventoryOutLot.builder()
                    .out(out)
                    .batch(em.getReference(InventoryBatch.class, p.getBatchId()))
                    .quantity(p.getQty())
                    .build();
            outLotRepo.save(lot);
        }

        // 7) inventory 테이블 현재고 동기화
        inventoryService.syncInventoryQuantity(materialId, remain);

        return out.getId();
    }

    /** 출고 헤더 삭제. */
    @Transactional
    public void deleteOut(Long outId) {
        inventoryOutRepository.deleteById(outId);
    }
}
