package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryLogDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventoryLogSearchDTO;
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
        var rows = qf.selectFrom(v)
                .where(
                        v.materialId.eq(materialId),
                        v.date.goe(from),
                        v.date.lt(to)
                )
                .orderBy(v.date.desc(), v.logId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = qf.select(v.logId.count())
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

        var rows = qf.selectFrom(v)
                .where(
                        eqMaterial(materialId),
                        eqType(type),
                        betweenDate(from, to)
                )
                .orderBy(v.date.desc(), v.logId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = qf.select(v.logId.count())
                .from(v)
                .where(
                        eqMaterial(materialId),
                        eqType(type),
                        betweenDate(from, to)
                )
                .fetchOne();

        return new PageImpl<>(rows, pageable, total != null ? total : 0L);
    }

    // ================= DTO 전용 메서드 추가 =================

    /**
     * 재고 로그 검색 DTO 기반 조회 (InventoryLogDTO 페이지 리턴).
     *
     * 컨트롤러에서 InventoryLogSearchDTO 그대로 넘겨 쓰면 됨.
     */
    public Page<InventoryLogDTO> findLogDto(InventoryLogSearchDTO cond, Pageable pageable) {
        LocalDateTime from = (cond.getStartDate() != null)
                ? cond.getStartDate().atStartOfDay()
                : null;
        LocalDateTime to = (cond.getEndDate() != null)
                ? cond.getEndDate().plusDays(1).atStartOfDay()
                : null;

        List<InventoryLogView> rows = qf.selectFrom(v)
                .where(
                        eqMaterial(cond.getMaterialId()),
                        eqType(cond.getType()),
                        betweenDate(from, to)
                )
                .orderBy(v.date.desc(), v.logId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = qf.select(v.logId.count())
                .from(v)
                .where(
                        eqMaterial(cond.getMaterialId()),
                        eqType(cond.getType()),
                        betweenDate(from, to)
                )
                .fetchOne();

        List<InventoryLogDTO> dtoList = rows.stream()
                .map(this::toDto)
                .toList();

        return new PageImpl<>(dtoList, pageable, total != null ? total : 0L);
    }

    // helpers
    private BooleanExpression eqMaterial(Long materialId) {
        return materialId != null ? v.materialId.eq(materialId) : null;
    }

    private BooleanExpression eqType(String type) {
        return (type != null && !type.isBlank()) ? v.type.eq(type) : null;
    }

    private BooleanExpression betweenDate(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) return v.date.goe(from).and(v.date.lt(to)); // 상한 미포함
        if (from != null) return v.date.goe(from);
        if (to != null)   return v.date.lt(to);
        return null;
    }

    private InventoryLogDTO toDto(InventoryLogView row) {
        return InventoryLogDTO.builder()
                .logId(row.getLogId())
                .logDate(row.getDate())
                .logType(row.getType())
                .quantity(row.getQuantity())
                .stockAfter(row.getStockAfter())
                .unitPrice(row.getUnitPrice())
                .memo(row.getMemo())
                .storeId(row.getStoreId())
                .storeName(row.getStoreName())
                .batchId(row.getBatchId())
                .build();
    }
}
