package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOut;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryOut;
import com.boot.ict05_final_admin.domain.store.entity.QStore;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InventoryOutRepositoryImpl implements InventoryOutRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QInventoryOut o = QInventoryOut.inventoryOut;
    private static final QStore s = QStore.store;

    @Override
    public BigDecimal getTotalQuantity(Long outId) {
        BigDecimal v = queryFactory
                .select(o.quantity)
                .from(o)
                .where(o.id.eq(outId))
                .fetchOne();
        return v != null ? v : BigDecimal.ZERO;
    }

    @Override
    public Optional<InventoryOut> findByIdWithStore(Long id) {
        InventoryOut res = queryFactory
                .selectFrom(o)
                .leftJoin(o.store, s).fetchJoin()
                .where(o.id.eq(id))
                .fetchOne();
        return Optional.ofNullable(res);
    }
}
