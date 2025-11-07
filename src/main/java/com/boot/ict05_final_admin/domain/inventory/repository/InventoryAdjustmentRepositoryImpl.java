package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryAdjustment;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryAdjustment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class InventoryAdjustmentRepositoryImpl implements InventoryAdjustmentRepositoryCustom {

    private final JPAQueryFactory qf;
    private static final QInventoryAdjustment a = QInventoryAdjustment.inventoryAdjustment;

    @Override
    public List<InventoryAdjustment> findByInventoryAndPeriod(Long inventoryId, LocalDateTime from, LocalDateTime to) {
        return qf.selectFrom(a)
                .where(
                        a.inventory.id.eq(inventoryId),
                        a.createdAt.between(from, to)   // 엔티티가 adjustDate/logDate이면 해당 필드로 교체
                )
                .orderBy(a.createdAt.desc(), a.id.desc())
                .fetch();
    }

    @Override
    public BigDecimal sumQuantityByInventoryAndPeriod(Long inventoryId, LocalDateTime from, LocalDateTime to) {
        BigDecimal v = qf.select(a.difference.sum())
                .from(a)
                .where(
                        a.inventory.id.eq(inventoryId),
                        a.createdAt.between(from, to)
                )
                .fetchOne();
        return v != null ? v : BigDecimal.ZERO;
    }

    @Override
    public LocalDateTime lastAdjustmentAtByInventory(Long inventoryId) {
        return qf.select(a.createdAt.max())
                .from(a)
                .where(a.inventory.id.eq(inventoryId))
                .fetchOne();
    }
}
