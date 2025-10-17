package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
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

import java.util.Arrays;
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
        // BooleanExpression condition = null;
        // 기본값 true
        BooleanExpression condition = Expressions.asBoolean(true).isTrue();

        // type이 없으면 all로 기본 처리
        String type = dto.getType() == null ? "all" : dto.getType();
        String keyword = dto.getS();

        if (keyword != null && !keyword.isEmpty()) {
            // 한글 설명을 enum name으로 매핑
            String matchedCategoryName = Arrays.stream(MaterialCategory.values())
                    .filter(c -> c.getDescription().contains(keyword))
                    .map(Enum::name)
                    .findFirst()
                    .orElse(null);

            switch (type) {
                case "title":
                    condition = material.title.containsIgnoreCase(keyword);
                    break;
                case "content":
                    condition = material.supplier.containsIgnoreCase(keyword);
                    break;
                case "category":
                    if (matchedCategoryName != null)
                        condition = material.materialCategory.stringValue().eq(matchedCategoryName);
                    else
                        condition = Expressions.asBoolean(false).isTrue(); // 매칭 안 되면 결과 없음
                    break;

                case "all":
                default:
                    BooleanExpression keywordCondition =
                            material.title.containsIgnoreCase(keyword)
                                    .or(material.supplier.containsIgnoreCase(keyword));

                    if (matchedCategoryName != null)
                        keywordCondition = keywordCondition
                                .or(material.materialCategory.stringValue().eq(matchedCategoryName));

                    condition = condition.and(keywordCondition);
                    break;
            }
        }

        if (dto.getStatus() != null && !dto.getStatus().toString().trim().isEmpty()) {
            condition = condition.and(material.materialStatus.eq(dto.getStatus()));
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
