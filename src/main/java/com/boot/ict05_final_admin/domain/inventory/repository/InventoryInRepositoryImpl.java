package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryIn;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryIn;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class InventoryInRepositoryImpl implements InventoryInRepositoryCustom {
    private final JPAQueryFactory qf;
    private static final QInventoryIn i = QInventoryIn.inventoryIn;

    @Override
    public List<InventoryIn> findRecentByMaterial(Long materialId, int limit) {
        return qf.selectFrom(i)
                .where(i.material.id.eq(materialId))
                .orderBy(i.inDate.desc(), i.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<InventoryIn> findByMaterialAndPeriod(Long materialId, LocalDateTime from, LocalDateTime to) {
        return qf.selectFrom(i)
                .where(i.material.id.eq(materialId), i.inDate.between(from, to))
                .orderBy(i.inDate.desc(), i.id.desc())
                .fetch();
    }
}
