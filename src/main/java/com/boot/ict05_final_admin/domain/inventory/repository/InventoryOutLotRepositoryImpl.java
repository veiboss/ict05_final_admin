package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOutLot;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryBatch;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryOut;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryOutLot;
import com.boot.ict05_final_admin.domain.store.entity.QStore;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class InventoryOutLotRepositoryImpl implements InventoryOutLotRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QInventoryOutLot l = QInventoryOutLot.inventoryOutLot;
    private static final QInventoryOut o = QInventoryOut.inventoryOut;
    private static final QInventoryBatch b = QInventoryBatch.inventoryBatch;
    private static final QStore s = QStore.store;

    @Override
    public List<InventoryOutLot> findByOutId(Long outId) {
        return queryFactory
                .selectFrom(l)
                .leftJoin(l.batch, b).fetchJoin()
                .where(l.out.id.eq(outId))
                .orderBy(l.id.asc())
                .fetch();
    }

    @Override
    public List<InventoryOutLot> findAllByBatchIdWithOutAndStore(Long batchId) {
        return queryFactory
                .selectFrom(l)
                .join(l.out, o).fetchJoin()
                .leftJoin(o.store, s).fetchJoin()
                .where(l.batch.id.eq(batchId))
                .orderBy(l.id.asc())
                .fetch();
    }

    @Override
    public List<Tuple> sumByBatchAndStoreForMaterial(Long materialId) {
        return queryFactory
                .select(b.id, o.store.id, l.quantity.sum())
                .from(l)
                .join(l.batch, b)
                .join(l.out, o)
                .where(b.material.id.eq(materialId))
                .groupBy(b.id, o.store.id)
                .fetch();
    }

    @Override
    public boolean existsByOutIdAndBatchId(Long outId, Long batchId) {
        Integer one = queryFactory
                .selectOne()
                .from(l)
                .where(
                        l.out.id.eq(outId),
                        l.batch.id.eq(batchId)
                )
                .fetchFirst();
        return one != null;
    }

    @Override
    public BigDecimal sumQuantityByOutId(Long outId) {
        BigDecimal v = queryFactory
                .select(l.quantity.sum())
                .from(l)
                .where(l.out.id.eq(outId))
                .fetchOne();
        return v != null ? v : BigDecimal.ZERO;
    }
}