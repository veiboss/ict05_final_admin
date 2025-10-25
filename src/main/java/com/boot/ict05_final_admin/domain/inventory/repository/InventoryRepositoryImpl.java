package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.QHqInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 본사 재고(Inventory) 커스텀 Repository 구현체.
 */
@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<InventoryListDTO> listInventory(InventorySearchDTO searchDTO, Pageable pageable) {
        QHqInventory inv = QHqInventory.hqInventory;
        QMaterial material = QMaterial.material;

        List<InventoryListDTO> content = queryFactory
                .select(Projections.fields(InventoryListDTO.class,
                        inv.id,
                        material.name.as("materialName"),
                        material.materialCategory.stringValue().as("categoryName"),
                        inv.quantity,
                        inv.optimalQuantity,
                        inv.status,
                        inv.updateDate))
                .from(inv)
                .join(inv.material, material)
                .where(applyFilter(searchDTO))
                .orderBy(inv.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = countInventory(searchDTO);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countInventory(InventorySearchDTO searchDTO) {
        QHqInventory inv = QHqInventory.hqInventory;
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
        QHqInventory inv = QHqInventory.hqInventory;
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
