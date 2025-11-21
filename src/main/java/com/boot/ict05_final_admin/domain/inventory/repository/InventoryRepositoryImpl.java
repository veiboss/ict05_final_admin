package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 본사 재고 커스텀 리포지토리 구현(QueryDSL).
 */
@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepositoryCustom {

    private final JPAQueryFactory qf;

    private static final QInventory inv = QInventory.inventory;
    private static final QMaterial  m   = QMaterial.material;

    @Override
    public Page<InventoryListDTO> listInventory(InventorySearchDTO dto, Pageable pageable) {
        // 정렬 힌트: 클라이언트가 넘기지 않으면 updateDate DESC
        Sort sort = (pageable != null && pageable.getSort().isSorted())
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "updateDate");
        Pageable p = PageRequest.of(
                pageable != null ? pageable.getPageNumber() : 0,
                pageable != null ? pageable.getPageSize() : 20,
                sort
        );

        List<InventoryListDTO> content = qf
                .select(Projections.fields(InventoryListDTO.class,
                        inv.id,
                        m.id.as("materialId"),
                        m.name.as("materialName"),
                        m.materialCategory.stringValue().as("categoryName"),
                        inv.quantity,
                        m.optimalQuantity.as("optimalQuantity"),
                        m.salesUnit.as("materialSalesUnit"),
                        inv.status,
                        inv.updateDate))
                .from(inv)
                .join(inv.material, m)
                .where(filter(dto))
                .orderBy(inv.updateDate.desc(), inv.id.desc())
                .offset(p.getOffset())
                .limit(p.getPageSize())
                .fetch();

        // 상태 재계산(요청 사항 반영)
        for (InventoryListDTO row : content) {
            row.setStatus(InventoryStatus.calculate(row.getQuantity(), row.getOptimalQuantity()));
        }

        long total = countInventory(dto);
        return new PageImpl<>(content, p, total);
    }

    @Override
    public long countInventory(InventorySearchDTO dto) {
        Long total = qf.select(inv.count())
                .from(inv)
                .join(inv.material, m)
                .where(filter(dto))
                .fetchOne();
        return total != null ? total : 0L;
    }

    // ----- helpers -----

    private BooleanExpression filter(InventorySearchDTO dto) {
        BooleanExpression w = Expressions.TRUE.isTrue();
        if (dto == null) return w;

        // 검색어(s): 재료명 / 카테고리명
        if (dto.getS() != null && !dto.getS().isBlank()) {
            String s = dto.getS();
            w = w.and(
                    m.name.containsIgnoreCase(s)
                            .or(m.materialCategory.stringValue().containsIgnoreCase(s))
            );
        }

        // 상태
        if (dto.getStatus() != null) {
            w = w.and(inv.status.eq(dto.getStatus()));
        }

        return w;
    }
}
