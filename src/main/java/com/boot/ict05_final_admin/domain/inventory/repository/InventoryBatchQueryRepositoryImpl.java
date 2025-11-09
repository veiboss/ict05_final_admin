package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.BatchStatusRowDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.FifoCandidateDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryBatch;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class InventoryBatchQueryRepositoryImpl implements InventoryBatchQueryRepository {

    private final JPAQueryFactory qf;

    @Override
    public List<BatchStatusRowDTO> findBatchStatusByMaterial(Long materialId) {
        var b = QInventoryBatch.inventoryBatch;
        return qf.select(Projections.constructor(
                        BatchStatusRowDTO.class,
                        b.id, b.lotNo, b.receivedDate, b.expirationDate,
                        b.receivedQuantity, b.quantity, b.unitPrice
                ))
                .from(b)
                .where(b.material.id.eq(materialId))
                .orderBy(b.expirationDate.asc().nullsLast(), b.receivedDate.asc(), b.id.asc())
                .fetch();
    }

    @Override
    public List<FifoCandidateDTO> findAvailableBatchesForFifo(Long materialId) {
        var b = QInventoryBatch.inventoryBatch;
        return qf.select(Projections.constructor(
                        FifoCandidateDTO.class,
                        b.id,               // batchId
                        b.lotNo,            // lotNo
                        b.expirationDate,   // expirationDate(LocalDate)
                        b.quantity          // available(잔량 컬럼)
                ))
                .from(b)
                .where(
                        b.material.id.eq(materialId),
                        b.quantity.gt(java.math.BigDecimal.ZERO)
                )
                // FIFO 기준: 먼저 들어온 순(입고일 ↑), 동일일이면 PK ↑
                .orderBy(b.receivedDate.asc(), b.id.asc())
                .fetch();
    }
}
