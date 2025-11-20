package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryOutPreviewItemDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.*;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchQueryRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutLotRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutRepository;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderDetailDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderItemDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.repository.ReceiveOrderRepository;
import com.boot.ict05_final_admin.domain.store.entity.Store;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class InventoryOutService {

    private final InventoryBatchQueryRepository inventoryBatchQueryRepository;
    private final InventoryOutRepository inventoryOutRepository;
    private final InventoryOutLotRepository inventoryOutLotRepository;
    private final InventoryBatchRepository inventoryBatchRepository;

    private final EntityManager em;

    private final UnitPriceService unitPriceService;
    private final InventoryService inventoryService;


    /**
     * 출고 미리보기.
     *
     * @param materialId 재료 ID
     * @param qty        총 출고 수량
     * @return 배치 분할 미리보기 결과
     */
    public List<InventoryOutPreviewItemDTO> previewFifo(Long materialId, BigDecimal qty) {
        // 현재 재고 조회
        BigDecimal currentStock = inventoryService.hqRemainOfMaterial(materialId);
        if (currentStock == null) currentStock = BigDecimal.ZERO;

        // 주문 수량과 비교
        if (currentStock.compareTo(qty) < 0) {
            throw new IllegalArgumentException("주문 수량이 현재 재고를 초과합니다. 현재 재고: " +
                    currentStock + ", 주문 수량: " + qty);
        }

        // FIFO 계획 생성
        var candidates = inventoryBatchQueryRepository.findAvailableBatchesForFifo(materialId);
        var remain = qty;
        List<InventoryOutPreviewItemDTO> plan = new ArrayList<>();

        for (var c : candidates) {
            if (remain.signum() <= 0) break;
            var take = c.getAvailable().min(remain);
            if (take.signum() > 0) {
                plan.add(InventoryOutPreviewItemDTO.builder()
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
     * 재고 수량을 확인하여 출고할 수 있는지 체크.
     *
     * @param materialId 재료 ID
     * @return 재고 수량
     */
    public BigDecimal hqRemainOfMaterial(Long materialId) {
        return inventoryService.hqRemainOfMaterial(materialId);
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

        LocalDateTime ts = (outDate != null) ? outDate : LocalDateTime.now();

        // 1) 현재 HQ 재고
        BigDecimal currentStock = inventoryService.hqRemainOfMaterial(materialId);
        if (currentStock == null) currentStock = BigDecimal.ZERO;
        log.info("[confirmOut] materialId={}, storeId={}, totalQty={}, currentStock={}",
                materialId, storeId, totalQty, currentStock);
        if (currentStock.compareTo(totalQty) < 0) {
            throw new IllegalArgumentException("출고 수량이 현재고를 초과합니다. current=" +
                    currentStock + ", out=" + totalQty);
        }

        // 2) FIFO plan
        List<InventoryOutPreviewItemDTO> plan = previewFifo(materialId, totalQty);

        BigDecimal plannedSum = BigDecimal.ZERO;
        for (InventoryOutPreviewItemDTO p : plan) {
            plannedSum = plannedSum.add(p.getQty());
            log.info("[confirmOut] plan item batchId={}, qty={}", p.getBatchId(), p.getQty());


            // 배치 조회 + 차감 + 저장
            InventoryBatch batch = inventoryBatchRepository.findById(p.getBatchId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("배치를 찾을 수 없습니다. id=" + p.getBatchId()));

            batch.subtractQuantity(p.getQty());
            inventoryBatchRepository.save(batch); // DynamicUpdate 덕분에 quantity만 UPDATE
        }

        log.info("[confirmOut] plannedSum={}, requested={}", plannedSum, totalQty);

        if (plannedSum.compareTo(totalQty) != 0) {
            throw new IllegalStateException("FIFO 분할 합계가 요청 수량과 일치하지 않습니다. planned=" +
                    plannedSum + ", requested=" + totalQty);
        }

        // 3) 배치 차감 이후 현재고 재계산
        BigDecimal remain = inventoryService.hqRemainOfMaterial(materialId);

        // 4) 단가 결정
        BigDecimal unitPrice = resolveOutUnitPrice(materialId, ts);

        // 가격 추가
        unitPriceService.addPricesForMaterial(materialId, unitPrice, unitPrice);  // 출고가는 매입가와 동일

        // 5) 헤더 생성
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

        // 6) LOT 생성 (plan 기준으로만 생성)
        for (InventoryOutPreviewItemDTO p : plan) {
            InventoryOutLot lot = InventoryOutLot.builder()
                    .out(out)
                    .batch(em.getReference(InventoryBatch.class, p.getBatchId()))
                    .quantity(p.getQty())
                    .build();
            inventoryOutLotRepository.save(lot);
        }

        // 7) inventory 테이블 현재고 동기화
        inventoryService.syncInventoryQuantity(materialId, remain);

        return out.getId();
    }

    /**
     * 수주 기반 본사 → 가맹점 출고 생성 유즈케이스
     *
     * <p>
     * 수주 상세 DTO({@link ReceiveOrderDetailDTO})와 그 하위 품목 DTO({@link ReceiveOrderItemDTO})를 기반으로
     * 각 자재별로 {@link #confirmOut(Long, Long, BigDecimal, LocalDateTime, String)} 을 호출하여
     * 출고를 생성한다.
     * </p>
     *
     * <p>
     * 여러 자재가 포함된 수주의 경우 자재별로 여러 개의 {@link InventoryOut} 헤더가
     * 생성될 수 있으며, 이 메서드는 그 중 첫 번째 헤더를 반환한다.
     * (주요 효과는 재고 차감 및 출고/배치 로그 생성이다.)
     * </p>
     *
     * @param orderDetail 출고 대상으로 하는 수주 상세 DTO (헤더 + 아이템 목록 포함)
     * @return 생성된 출고 헤더 중 첫 번째 엔티티
     */
    @Transactional
    public InventoryOut createOutByReceiveOrder(ReceiveOrderDetailDTO orderDetail) {
        if (orderDetail == null) {
            throw new IllegalArgumentException("수주 상세 정보가 null 입니다.");
        }

        List<ReceiveOrderItemDTO> items = orderDetail.getItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("수주 상세 품목이 없습니다. orderId=" + orderDetail.getId()
                    + ", orderCode=" + orderDetail.getOrderCode());
        }

        Long storeId = orderDetail.getStoreId();
        String baseMemo = "수주 자동 출고: " + orderDetail.getOrderCode();
        LocalDateTime outDate = LocalDateTime.now();

        InventoryOut firstOut = null;

        for (ReceiveOrderItemDTO item : items) {
            if (item == null) {
                continue;
            }

            Integer cnt = item.getDetailCount();
            if (cnt == null || cnt <= 0) {
                continue;
            }

            Long materialId = item.getMaterialId();
            if (materialId == null) {
                throw new IllegalStateException(
                        "수주 상세 품목에 재료 ID가 없습니다. orderId=" + orderDetail.getId()
                                + ", orderCode=" + orderDetail.getOrderCode()
                                + ", itemName=" + item.getName()
                );
            }

            BigDecimal qty = BigDecimal.valueOf(cnt.longValue());

            Long outId = confirmOut(materialId, storeId, qty, outDate, baseMemo);

            if (firstOut == null) {
                firstOut = inventoryOutRepository.findById(outId)
                        .orElseThrow(() ->
                                new IllegalStateException("출고 헤더를 찾을 수 없습니다. id=" + outId));
            }
        }

        if (firstOut == null) {
            throw new IllegalStateException(
                    "출고 대상 수량이 없습니다. orderId=" + orderDetail.getId()
                            + ", orderCode=" + orderDetail.getOrderCode()
            );
        }

        return firstOut;
    }


    /** 출고 헤더 삭제. */
    @Transactional
    public void deleteOut(Long outId) {
        inventoryOutRepository.deleteById(outId);
    }
}
