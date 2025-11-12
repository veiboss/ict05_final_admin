package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.QStoreInventory;
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
 * 가맹점 재고(StoreInventory) 커스텀 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class StoreInventoryRepositoryImpl implements StoreInventoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<StoreInventoryListDTO> listStoreInventory(StoreInventorySearchDTO storeInventorySearchDTO, Pageable pageable) {
        QStoreInventory si = QStoreInventory.storeInventory;
        QStoreMaterial sm = QStoreMaterial.storeMaterial;
        QStore store = QStore.store;

        List<StoreInventoryListDTO> content = queryFactory
                .select(Projections.fields(StoreInventoryListDTO.class,
                        si.id.as("id"),
                        store.id.as("storeId"),
                        store.name.as("storeName"),
                        Expressions.stringTemplate("COALESCE({0}, {1})", sm.material.name, sm.name).as("materialName"),
                        Expressions.stringTemplate("COALESCE({0}, {1})", sm.material.materialCategory.stringValue(), sm.category).as("categoryName"),
                        si.quantity.as("quantity"),
                        si.optimalQuantity.as("optimalQuantity"),
                        si.status.as("status"),
                        si.updateDate.as("updateDate")
                ))
                .from(si)
                .join(si.storeMaterial, sm)
                .leftJoin(sm.material)
                .join(si.store, store)
                .where(applyFilter(storeInventorySearchDTO))
                .orderBy(si.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = countStoreInventory(storeInventorySearchDTO);
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 재고 총 개수 조회
     */
    @Override
    public long countStoreInventory(StoreInventorySearchDTO storeInventorySearchDTO) {
        QStoreInventory si = QStoreInventory.storeInventory;
        QStoreMaterial sm = QStoreMaterial.storeMaterial;
        QStore store = QStore.store;

        Long total = queryFactory
                .select(si.count())
                .from(si)
                .join(si.storeMaterial, sm)
                .leftJoin(sm.material)
                .join(si.store, store)
                .where(applyFilter(storeInventorySearchDTO))
                .fetchOne();

        return total != null ? total : 0L;
    }

    /**
     * 검색 필터 구성
     */
    private BooleanExpression applyFilter(StoreInventorySearchDTO dto) {
        QStoreInventory si = QStoreInventory.storeInventory;
        QStoreMaterial sm = QStoreMaterial.storeMaterial;
        QStore store = QStore.store;

        BooleanExpression condition = Expressions.asBoolean(true).isTrue();

        // 검색어 (재료명, 카테고리명)
        if (dto.getS() != null && !dto.getS().isEmpty()) {
            condition = condition.and(
                    sm.material.name.containsIgnoreCase(dto.getS())
                            .or(sm.material.materialCategory.stringValue().containsIgnoreCase(dto.getS()))
            );
        }

        // 재고 상태 필터
        if (dto.getStatus() != null) {
            condition = condition.and(si.status.eq(dto.getStatus()));
        }

        // 가맹점 ID 필터
        if (dto.getStoreId() != null) {
            condition = condition.and(store.id.eq(dto.getStoreId()));
        }

        return condition;
    }
}
