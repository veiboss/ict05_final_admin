package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 본사 재고(InventoryBase) 커스텀 Repository 구현체.
 * 분리 완료로 역할 종료. 즉시 삭제 가능.
 */
@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<InventoryListDTO> listInventory(InventorySearchDTO searchDTO, Pageable pageable) {
        QInventory inv = QInventory.inventory;
        QMaterial material = QMaterial.material;

        List<InventoryListDTO> content = queryFactory
                .select(Projections.fields(InventoryListDTO.class,
                        inv.id,
                        material.id.as("materialId"),
                        material.name.as("materialName"),
                        material.materialCategory.stringValue().as("categoryName"),
                        inv.quantity,
                        material.optimalQuantity.as("optimalQuantity"),
                        material.salesUnit.as("materialSalesUnit"),
                        inv.status,
                        inv.updateDate))
                .from(inv)
                .join(inv.material, material)
                .where(applyFilter(searchDTO))
                .orderBy(inv.updateDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 상태 재계산: Java 레벨에서 InventoryStatus.calculate() 호출
        for (InventoryListDTO dto : content) {
            dto.setStatus(
                    InventoryStatus.calculate(dto.getQuantity(), dto.getOptimalQuantity())
            );
        }

        long total = countInventory(searchDTO);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countInventory(InventorySearchDTO searchDTO) {
        QInventory inv = QInventory.inventory;
        QMaterial material = QMaterial.material;

        Long total = queryFactory
                .select(inv.count())
                .from(inv)
                .join(inv.material, material)
                .where(applyFilter(searchDTO))
                .fetchOne();

        return total != null ? total : 0L;
    }

    /**
     * 검색 필터 구성
     */
    private BooleanExpression applyFilter(InventorySearchDTO dto) {
        QInventory inv = QInventory.inventory;
        QMaterial material = QMaterial.material;

        BooleanExpression condition = Expressions.asBoolean(true).isTrue();

        // 검색어(s)
        if (dto.getS() != null && !dto.getS().isEmpty()) {
            condition = condition.and(
                    material.name.containsIgnoreCase(dto.getS())
                            .or(material.materialCategory.stringValue().containsIgnoreCase(dto.getS()))
            );
        }

        // 상태 필터
        if (dto.getStatus() != null) {
            condition = condition.and(inv.status.eq(dto.getStatus()));
        }

        return condition;
    }
}
