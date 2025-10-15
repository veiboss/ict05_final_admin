package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialTemperature;
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

        // 데이터 목록 조회
        List<MaterialListDTO> content = queryFactory
                .select(Projections.fields(MaterialListDTO.class,
                        material.id,
                        material.code,
                        material.category,
                        material.title,
                        material.unit,
                        material.supplier,
                        material.materialTemperature,
                        material.materialStatus
                )) // member.name 매핑
                .from(material)
                .where(
                        eqTitleOrBody(materialSearchDTO, material)
                )
                .orderBy(material.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 조회
        long total = queryFactory
                .select(material.count())
                .from(material)
                .where(
                        eqTitleOrBody(materialSearchDTO, material)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }


    private BooleanExpression eqTitleOrBody(MaterialSearchDTO materialSearchDTO, QMaterial material) {
        if (materialSearchDTO.getType() == null || materialSearchDTO.getS() == null) {
            return null; // 조건 없음
        }

        String keyword = materialSearchDTO.getS();

        switch (materialSearchDTO.getType()) {
            case "title":
                return material.title.containsIgnoreCase(keyword);
            case "content":
                return material.supplier.containsIgnoreCase(keyword);
            case "all": // title + body 모두 검색
                return material.title.containsIgnoreCase(keyword)
                        .or(material.supplier.containsIgnoreCase(keyword));
            default:
                return null;
        }
    }

    @Override
    public long countMaterial(MaterialSearchDTO materialSearchDTO) {
        QMaterial material = QMaterial.material;

        long total = queryFactory
                .select(material.count())
                .from(material)
                .where(
                        eqTitleOrBody(materialSearchDTO, material)
                )
                .fetchOne();

        return total;
    }
}
