package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MaterialRepositoryImpl implements MaterialRepositoryCustom{


    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MaterialListDTO> listMaterial(MaterialSearchDTO materialSearchDTO, Pageable pageable) {
        QMaterial material = QMaterial.material;

        List<MaterialListDTO> content = queryFactory
                .select(Projections.fields(MaterialListDTO.class,
                        material.id,
                        material.code,
                        material.materialCategory,
                        material.title,
                        material.unit,
                        material.supplier,
                        material.materialTemperature,
                        material.materialStatus
                ))
                .from(material)
                .where(eqMaterialFilter(materialSearchDTO, material))
                .orderBy(material.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(material.count())
                .from(material)
                .where(eqMaterialFilter(materialSearchDTO, material))
                .fetchFirst();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression eqMaterialFilter(MaterialSearchDTO dto, QMaterial material) {
        BooleanExpression condition = null;

        if (dto.getType() != null && dto.getS() != null) {
            String keyword = dto.getS();
            switch (dto.getType()) {
                case "title":
                    condition = material.title.containsIgnoreCase(keyword);
                    break;
                case "content":
                    condition = material.supplier.containsIgnoreCase(keyword);
                    break;
                case "all":
                    condition = material.title.containsIgnoreCase(keyword)
                            .or(material.supplier.containsIgnoreCase(keyword));
                    break;
            }
        }

        if (dto.getStatus() != null) {
            BooleanExpression statusCondition = material.materialStatus.eq(dto.getStatus());
            condition = (condition == null)
                    ? statusCondition
                    : condition.and(statusCondition);
        }

        return condition;
    }

    @Override
    public long countMaterial(MaterialSearchDTO materialSearchDTO) {
        QMaterial material = QMaterial.material;

        Long total = queryFactory
                .select(material.count())
                .from(material)
                .where(eqMaterialFilter(materialSearchDTO, material))
                .fetchFirst();

        return total != null ? total : 0L;
    }
}
