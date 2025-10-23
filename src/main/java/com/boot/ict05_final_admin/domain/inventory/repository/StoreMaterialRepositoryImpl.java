package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.QHqInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.QStoreInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.QStoreMaterial;
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

@Repository
@RequiredArgsConstructor
public class StoreMaterialRepositoryImpl implements StoreMaterialRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<StoreMaterialListDTO> listStoreMaterial(StoreMaterialSearchDTO searchDTO, Pageable pageable) {
        QStoreMaterial sm = QStoreMaterial.storeMaterial;

        List<StoreMaterialListDTO> content = queryFactory
                .select(Projections.fields(StoreMaterialListDTO.class,
                        sm.id,
                        sm.code,
                        sm.name,
                        sm.category,
                        sm.baseUnit,
                        sm.supplier,
                        sm.temperature,
                        sm.status,
                        sm.quantity,
                        sm.optimalQuantity,
                        sm.purchasePrice,
                        sm.sellingPrice,
                        sm.expirationDate,
                        sm.isHqMaterial
                ))
                .from(sm)
                .where(eqStoreMaterialFilter(searchDTO, sm))
                .orderBy(sm.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(sm.count())
                .from(sm)
                .where(eqStoreMaterialFilter(searchDTO, sm))
                .fetchFirst();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression eqStoreMaterialFilter(StoreMaterialSearchDTO dto, QStoreMaterial sm) {
        BooleanExpression condition = Expressions.asBoolean(true).isTrue();

        String type = dto.getType() == null ? "all" : dto.getType();
        String keyword = dto.getS();

        // 검색어 조건
        if (keyword != null && !keyword.isEmpty()) {
            switch (type) {
                case "title":
                    condition = sm.name.containsIgnoreCase(keyword);
                    break;
                case "content":
                    condition = sm.supplier.containsIgnoreCase(keyword);
                    break;
                case "all":
                default:
                    condition = sm.name.containsIgnoreCase(keyword)
                            .or(sm.code.containsIgnoreCase(keyword))
                            .or(sm.supplier.containsIgnoreCase(keyword));
                    break;
            }
        }

        // 상태 필터
        if (dto.getStatus() != null && !dto.getStatus().toString().trim().isEmpty()) {
            condition = condition.and(sm.status.eq(dto.getStatus()));
        }

        // 본사 재료 여부 필터
        if (dto.getIsHqMaterial() != null) {
            condition = condition.and(sm.isHqMaterial.eq(dto.getIsHqMaterial()));
        }

        // 가맹점 ID 필터
        if (dto.getStoreId() != null) {
            condition = condition.and(sm.store.id.eq(dto.getStoreId()));
        }

        return condition;
    }

    @Override
    public long countStoreMaterial(StoreMaterialSearchDTO searchDTO) {
        QStoreMaterial sm = QStoreMaterial.storeMaterial;

        Long total = queryFactory
                .select(sm.count())
                .from(sm)
                .where(eqStoreMaterialFilter(searchDTO, sm))
                .fetchFirst();

        return total != null ? total : 0L;
    }
}
