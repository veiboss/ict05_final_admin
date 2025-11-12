package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryBatch;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class InventoryBatchRepositoryImpl implements InventoryBatchRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    private static final QInventoryBatch b = QInventoryBatch.inventoryBatch;

    @Override
    public List<InventoryBatch> findHqBatchesForMaterial(Long materialId) {
        List<InventoryBatch> rows = queryFactory
                .selectFrom(b)
                .where(
                        b.material.id.eq(materialId),
                        b.store.isNull(),
                        b.quantity.gt(BigDecimal.ZERO)
                )
                .orderBy(
                        // null은 뒤로(= not-null 먼저)
                        new OrderSpecifier<>(Order.ASC, b.expirationDate, OrderSpecifier.NullHandling.NullsLast),
                        b.receivedDate.asc()
                )
                .fetch();
        return rows != null ? rows : java.util.Collections.emptyList();
    }

    @Override
    public List<InventoryBatch> findFifoCandidates(Long materialId) {
        return findHqBatchesForMaterial(materialId);
    }

    @Override
    @Transactional
    public int decrementQuantity(Long batchId, BigDecimal delta) {
        long updated = queryFactory
                .update(b)
                .set(b.quantity, b.quantity.subtract(delta))
                .where(
                        b.id.eq(batchId),
                        b.quantity.goe(delta)
                )
                .execute();
        return (int) updated;
    }

    @Override
    public long countByMaterialAndDate(Long materialId, LocalDate targetDate) {
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        Long cnt = queryFactory
                .select(b.id.count())
                .from(b)
                .where(
                        b.material.id.eq(materialId),
                        b.store.isNull(),
                        b.receivedDate.goe(start),
                        b.receivedDate.lt(end)
                )
                .fetchOne();
        return cnt != null ? cnt : 0L;
    }

}
