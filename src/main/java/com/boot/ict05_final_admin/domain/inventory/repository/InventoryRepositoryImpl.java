package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.QHqInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
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
 * 재고 커스텀 Repository 구현체.
 */
@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<InventoryListDTO> listInventory(InventorySearchDTO searchDTO, Pageable pageable) {
        if ("STORE".equalsIgnoreCase(searchDTO.getInventoryType())) {
            return listStoreInventory(searchDTO, pageable);
        } else {
            return listHqInventory(searchDTO, pageable);
        }
    }

    @Override
    public long countInventory(InventorySearchDTO searchDTO) {
        if ("STORE".equalsIgnoreCase(searchDTO.getInventoryType())) {
            return countStoreInventory(searchDTO);
        } else {
            return countHqInventory(searchDTO);
        }
    }

    // ==================== 본사 재고 ====================
    private Page<InventoryListDTO> listHqInventory(InventorySearchDTO searchDTO, Pageable pageable) {
        QHqInventory hq = QHqInventory.hqInventory;
        QMaterial material = QMaterial.material;

        List<InventoryListDTO> content = queryFactory
                .select(Projections.fields(InventoryListDTO.class,
                        hq.id,
                        material.name.as("materialName"),
                        material.materialCategory.stringValue().as("categoryName"),
                        hq.quantity,
                        hq.optimalQuantity,
                        hq.status,
                        hq.updateDate))
                .from(hq)
                .join(hq.material, material)
                .where(hqInventoryFilter(searchDTO))
                .orderBy(hq.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = countHqInventory(searchDTO);
        return new PageImpl<>(content, pageable, total);
    }

    private long countHqInventory(InventorySearchDTO searchDTO) {
        QHqInventory hq = QHqInventory.hqInventory;
        QMaterial material = QMaterial.material;

        Long total = queryFactory
                .select(hq.count())
                .from(hq)
                .join(hq.material, material)
                .where(hqInventoryFilter(searchDTO))
                .fetchFirst();

        return total != null ? total : 0L;
    }

    // ==================== 가맹점 재고 ====================
    private Page<InventoryListDTO> listStoreInventory(InventorySearchDTO searchDTO, Pageable pageable) {
        QStoreInventory inv = QStoreInventory.storeInventory;
        QStoreMaterial sm = QStoreMaterial.storeMaterial;
        QMaterial material = QMaterial.material;
        QStore store = QStore.store;

        List<InventoryListDTO> content = queryFactory
                .select(Projections.fields(InventoryListDTO.class,
                        inv.id,
                        material.name.as("materialName"),
                        material.materialCategory.stringValue().as("categoryName"),
                        inv.quantity,
                        inv.optimalQuantity,
                        inv.status,
                        inv.updateDate,
                        store.name.as("storeName")))
                .from(inv)
                .join(inv.storeMaterial, sm)
                .join(sm.material, material)
                .join(inv.store, store)
                .where(storeInventoryFilter(searchDTO))
                .orderBy(inv.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = countStoreInventory(searchDTO);
        return new PageImpl<>(content, pageable, total);
    }

    private long countStoreInventory(InventorySearchDTO searchDTO) {
        QStoreInventory inv = QStoreInventory.storeInventory;
        QStoreMaterial sm = QStoreMaterial.storeMaterial;
        QMaterial material = QMaterial.material;
        QStore store = QStore.store;

        Long total = queryFactory
                .select(inv.count())
                .from(inv)
                .join(inv.storeMaterial, sm)
                .join(sm.material, material)
                .join(inv.store, store)
                .where(storeInventoryFilter(searchDTO))
                .fetchFirst();

        return total != null ? total : 0L;
    }

    // ==================== 조건 필터 ====================
    private BooleanExpression hqInventoryFilter(InventorySearchDTO dto) {
        QHqInventory hq = QHqInventory.hqInventory;
        QMaterial material = QMaterial.material;
        BooleanExpression condition = Expressions.asBoolean(true).isTrue();

        if (dto.getMaterialName() != null && !dto.getMaterialName().isEmpty()) {
            condition = condition.and(material.name.containsIgnoreCase(dto.getMaterialName()));
        }
        if (dto.getCategoryName() != null && !dto.getCategoryName().isEmpty()) {
            condition = condition.and(material.materialCategory.stringValue().eq(dto.getCategoryName()));
        }
        if (dto.getStatus() != null) {
            condition = condition.and(hq.status.eq(dto.getStatus()));
        }
        return condition;
    }

    private BooleanExpression storeInventoryFilter(InventorySearchDTO dto) {
        QStoreInventory inv = QStoreInventory.storeInventory;
        QStoreMaterial sm = QStoreMaterial.storeMaterial;
        QMaterial material = QMaterial.material;
        BooleanExpression condition = Expressions.asBoolean(true).isTrue();

        if (dto.getMaterialName() != null && !dto.getMaterialName().isEmpty()) {
            condition = condition.and(material.name.containsIgnoreCase(dto.getMaterialName()));
        }
        if (dto.getCategoryName() != null && !dto.getCategoryName().isEmpty()) {
            condition = condition.and(material.materialCategory.stringValue().eq(dto.getCategoryName()));
        }
        if (dto.getStatus() != null) {
            condition = condition.and(inv.status.eq(dto.getStatus()));
        }
        if (dto.getStoreId() != null) {
            condition = condition.and(inv.store.id.eq(dto.getStoreId()));
        }
        return condition;
    }
}
