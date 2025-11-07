package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOutLot;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutLotRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 로트 현황/상세 조회 서비스 (InventoryLotService)
 *
 * <p>본사(HQ) 배치 현황과 단일 로트의 출고 이력을 제공한다.</p>
 */
@Service
@RequiredArgsConstructor
public class InventoryLotService {

    private final InventoryBatchRepository batchRepo;
    private final InventoryOutLotRepository outLotRepo;

    /**
     * 배치 현황 행 DTO
     */
    @Getter @Builder
    public static class BatchStatusRow {
        private Long batchId;
        private String lotNo;
        private LocalDateTime receivedDate;
        private LocalDate expirationDate;
        private BigDecimal receivedQty;
        private BigDecimal remainQty;
        private BigDecimal unitPrice;
    }

    /**
     * HQ 배치 현황 조회
     *
     * @param materialId 재료 ID
     * @return 배치 현황 리스트
     */
    @Comment("HQ 배치 현황")
    public List<BatchStatusRow> getBatchStatusForMaterial(Long materialId) {
        return batchRepo.findHqBatchesForMaterial(materialId).stream()
                .map(b -> BatchStatusRow.builder()
                        .batchId(b.getId())
                        .lotNo(b.getLotNo())
                        .receivedDate(b.getReceivedDate())
                        .expirationDate(b.getExpirationDate())
                        .receivedQty(b.getReceivedQuantity())
                        .remainQty(b.getQuantity())
                        .unitPrice(b.getUnitPrice())
                        .build())
                .toList();
    }

    /**
     * 단일 로트의 출고 이력 행 DTO
     */
    @Getter @Builder
    public static class BatchOutRow {
        private Long outId;
        private Long storeId;
        private String storeName;
        private BigDecimal qty;
        private LocalDateTime createdAt;
    }

    /**
     * 단일 로트 출고 이력 조회
     *
     * @param batchId 배치 ID
     * @return 출고 이력 리스트
     */
    @Comment("로트 출고 이력")
    public List<BatchOutRow> getBatchOutHistory(Long batchId) {
        List<InventoryOutLot> lots = outLotRepo.findAllByBatchIdWithOutAndStore(batchId);
        return lots.stream().map(l ->
                BatchOutRow.builder()
                        .outId(l.getOut().getId())
                        .storeId(l.getOut().getStore() != null ? l.getOut().getStore().getId() : null)
                        .storeName(l.getOut().getStore() != null ? l.getOut().getStore().getName() : "-")
                        .qty(l.getQuantity())
                        .createdAt(l.getCreatedAt())
                        .build()
        ).toList();
    }
}
