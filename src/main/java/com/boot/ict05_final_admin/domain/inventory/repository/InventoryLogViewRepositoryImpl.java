package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryLogView;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class InventoryLogViewRepositoryImpl implements InventoryLogViewRepositoryCustom {

    private final JPAQueryFactory qf;
    private static final QInventoryLogView v = QInventoryLogView.inventoryLogView;

    @Override
    public Page<InventoryLogView> pageByMaterialAndPeriod(Long materialId,
                                                          LocalDateTime from,
                                                          LocalDateTime to,
                                                          Pageable pageable) {

        List<InventoryLogView> rows = qf.selectFrom(v)
                .where(
                        v.materialId.eq(materialId),
                        v.date.goe(from),
                        v.date.lt(to)
                )
                .orderBy(v.date.desc(), v.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = qf.select(v.id.count())
                .from(v)
                .where(
                        v.materialId.eq(materialId),
                        v.date.goe(from),
                        v.date.lt(to)
                )
                .fetchOne();

        return new PageImpl<>(rows, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<InventoryLogView> findLogsByFilter(Long materialId,
                                                   String type,
                                                   LocalDate startDate,
                                                   LocalDate endDate,
                                                   Pageable pageable) {

        LocalDateTime from = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime to   = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : null;

        List<InventoryLogView> rows = qf.selectFrom(v)
                .where(
                        eqMaterial(materialId),
                        eqType(type),
                        betweenDate(from, to)
                )
                .orderBy(v.date.desc(), v.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = qf.select(v.id.count())
                .from(v)
                .where(
                        eqMaterial(materialId),
                        eqType(type),
                        betweenDate(from, to)
                )
                .fetchOne();

        return new PageImpl<>(rows, pageable, total != null ? total : 0L);
    }

    // helpers

    private BooleanExpression eqMaterial(Long materialId) {
        return materialId != null ? v.materialId.eq(materialId) : null;
    }

    private BooleanExpression eqType(String type) {
        return (type != null && !type.isBlank()) ? v.type.eq(type) : null; // enum이면 변환하여 eq(enum)
    }

    private BooleanExpression betweenDate(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) return v.date.goe(from).and(v.date.lt(to));
        if (from != null) return v.date.goe(from);
        if (to != null)   return v.date.lt(to);
        return null;
    }
}
