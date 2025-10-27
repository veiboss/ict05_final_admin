package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.boot.ict05_final_admin.domain.inventory.entity.QStoreMaterial;
import com.boot.ict05_final_admin.domain.store.entity.QStore;
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
 * 가맹점 재료(StoreMaterial) 커스텀 Repository 구현체.
 *
 * <p>QueryDSL 기반 검색 및 페이징 처리.</p>
 */
@Repository
@RequiredArgsConstructor
public class StoreMaterialRepositoryImpl implements StoreMaterialRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<StoreMaterialListDTO> listStoreMaterial(StoreMaterialSearchDTO searchDTO, Pageable pageable) {
        QStoreMaterial sm = QStoreMaterial.storeMaterial;
        QMaterial material = QMaterial.material;
        QStore store = QStore.store;

        List<StoreMaterialListDTO> content = queryFactory
                .select(Projections.fields(StoreMaterialListDTO.class,
                        sm.id,
                        material.code.as("code"),
                        material.name.as("name"),
                        material.materialCategory.as("materialCategory"),
                        material.baseUnit.as("baseUnit"),
                        material.salesUnit.as("salesUnit"),
                        material.conversionRate.as("conversionRate"),
                        material.supplier.as("supplier"),
                        material.materialStatus.as("status"),
                        sm.isHqMaterial.as("isHqMaterial"),
                        store.name.as("storeName")
                ))
                .from(sm)
                .join(sm.material, material)
                .join(sm.store, store)
                .where(applyFilter(searchDTO))
                .orderBy(sm.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = countStoreMaterial(searchDTO);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countStoreMaterial(StoreMaterialSearchDTO searchDTO) {
        QStoreMaterial sm = QStoreMaterial.storeMaterial;
        QMaterial material = QMaterial.material;
        QStore store = QStore.store;

        Long total = queryFactory
                .select(sm.count())
                .from(sm)
                .join(sm.material, material)
                .join(sm.store, store)
                .where(applyFilter(searchDTO))
                .fetchOne();

        return total != null ? total : 0L;
    }

    /**
     * 검색 조건 필터 (StoreMaterialSearchDTO 기반)
     */
    private BooleanExpression applyFilter(StoreMaterialSearchDTO dto) {
        QStoreMaterial sm = QStoreMaterial.storeMaterial;
        QMaterial material = QMaterial.material;
        QStore store = QStore.store;

        BooleanExpression condition = Expressions.asBoolean(true).isTrue();

        // 검색어 (재료명, 카테고리)
        if (dto.getS() != null && !dto.getS().isEmpty()) {
            condition = condition.and(
                    material.name.containsIgnoreCase(dto.getS())
                            .or(material.materialCategory.stringValue().containsIgnoreCase(dto.getS()))
            );
        }

        // 상태 필터
        if (dto.getStatus() != null) {
            condition = condition.and(material.materialStatus.eq(dto.getStatus()));
        }

        // 본사 재료 여부
        if (dto.getIsHqMaterial() != null) {
            condition = condition.and(sm.isHqMaterial.eq(dto.getIsHqMaterial()));
        }

        // 가맹점 ID
        if (dto.getStoreId() != null) {
            condition = condition.and(store.id.eq(dto.getStoreId()));
        }

        return condition;
    }
}
