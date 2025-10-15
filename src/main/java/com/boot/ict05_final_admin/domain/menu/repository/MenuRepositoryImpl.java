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

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MenuListDTO> listMenu(Pageable pageable) {
        QMenu menu = QMenu.menu;
        QMaterial material = QMaterial.material;
        QMenuCategoryEntity menuCategory = QMenuCategoryEntity.menuCategoryEntity;

        // 메뉴 목록 조회
        List<MenuListDTO> content = queryFactory
                .select(Projections.fields(MenuListDTO.class,
                        menu.menuId,
                        menu.menuCode,
                        menu.menuShow,
                        menu.menuName,
                        menu.menuPrice,
                        menu.menuKcal,
                        menuCategory.menuCategoryName
                        ))
                .from(menu)
                .join(menu.menuCategory, menuCategory)
                .orderBy(menu.menuId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 조회
        long total = queryFactory
                .select(menu.menuId.countDistinct())
                .from(menu)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);


    }


}
