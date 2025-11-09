package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.BatchOutRowDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryOut;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryOutLot;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryOutLotQueryRepositoryImpl implements InventoryOutLotQueryRepository {

    private final JPAQueryFactory qf;
    private final com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutLotRepository outLotRepo;

    @Override
    public Page<BatchOutRowDTO> pageOutHistoryByBatch(Long batchId, Pageable pageable) {
        var ol = QInventoryOutLot.inventoryOutLot;
        var o  = QInventoryOut.inventoryOut;

        Long total = qf.select(ol.id.count())
                .from(ol)
                .where(ol.batch.id.eq(batchId))
                .fetchOne();
        long totalCount = total != null ? total : 0L;

        var rows = qf.select(Projections.constructor(
                        BatchOutRowDTO.class,
                        o.id,
                        o.store.id,
                        o.store.name,
                        ol.quantity,
                        o.outDate
                ))
                .from(ol)
                .join(ol.out, o)
                .where(ol.batch.id.eq(batchId))
                .orderBy(o.outDate.desc(), ol.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new org.springframework.data.domain.PageImpl<>(rows, pageable, totalCount);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteOutById(Long lotId) {
        outLotRepo.deleteById(lotId);
    }
}
