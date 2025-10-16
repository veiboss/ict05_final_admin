package com.boot.ict05_final_admin.domain.menu.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.boot.ict05_final_admin.domain.menu.dto.MenuListDTO;
import com.boot.ict05_final_admin.domain.menu.entity.QMenu;
import com.boot.ict05_final_admin.domain.menu.entity.QMenuCategoryEntity;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.spel.ast.Projection;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom {

    private final JPAQueryFactory queryFactory;     // QueryDSL의 핵심 객체

    @Override
    public Page<MenuListDTO> listMenu(Pageable pageable) {
        QMenu menu = QMenu.menu;        // 각 엔티티의 QueryDSL 전용 메타 클래스(QueryDSL이 자동으로 변환해서 만든 쿼리용 전용 클래스)
        // QMaterial material = QMaterial.material;
        QMenuCategoryEntity menuCategory = QMenuCategoryEntity.menuCategoryEntity;

        // 메뉴 목록 조회
        List<MenuListDTO> content = queryFactory
                    .select(Projections.fields(MenuListDTO.class,   // DTO의 필드명과 선택한 컬럼의 별칭이 반드시 일치
                        menu.menuId.as("menuId"),
                        menu.menuCode.as("menuCode"),
                        menu.menuShow.as("menuShow"),
                        menu.menuName.as("menuName"),
                        menu.menuPrice.as("menuPrice"),
                        menu.menuKcal.as("menuKcal"),
                        menuCategory.menuCategoryName.as("menuCategoryName")
                        ))
                .from(menu)
                .join(menu.menuCategory, menuCategory)
                .orderBy(menu.menuId.desc())
                .offset(pageable.getOffset())   // 몇 번째부터
                .limit(pageable.getPageSize())  // 몇 개 가져올지
                .fetch();   // 실제로 DB에 쿼리를 보내서 결과를 가져오라 명령

        // 전체 카운트 조회
        long total = queryFactory   // 쿼리를 만드는 객체
                .select(menu.menuId.countDistinct())    // 중복없이 개수 셈
                .from(menu)
                .fetchOne();    // 결과 1개만 가져옴

        // 전체 페이지 계산용 Page 객체 반환
        return new PageImpl<>(content, pageable, total);    // PageImpl은 Spring이 제공하는 기본 Page 구현체
    }


}
