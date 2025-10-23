package com.boot.ict05_final_admin.domain.menu.repository;

import com.boot.ict05_final_admin.domain.menu.dto.MenuListDTO;
import com.boot.ict05_final_admin.domain.menu.dto.MenuSearchDTO;
import com.boot.ict05_final_admin.domain.menu.entity.*;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MenuListDTO> listMenu(MenuSearchDTO dto, Pageable pageable) {
        QMenu menu = QMenu.menu;
        QMenuCategory category = QMenuCategory.menuCategory;
        QMenuRecipe recipe = QMenuRecipe.menuRecipe;
        QMaterial material = QMaterial.material;

        // ===== WHERE 조건 =====
        BooleanExpression where = andAll(
                eqNameOrInfo(dto, menu),
                eqCategory(dto, menu),
                // 필요 시 판매상태 필터도 추가 가능
                null
        );

        // ===== 정렬 =====
        Sort sort = (pageable.getSort().isSorted()) ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "menuId");

        // ===== 목록 조회 + 주재료 리스트 집계(GroupBy) =====
        Map<Long, MenuListDTO> mapped = queryFactory
                .from(menu)
                .leftJoin(menu.menuCategory, category)
                .leftJoin(menu.recipe, recipe)
                .leftJoin(recipe.material, material)
                .where(where)
                // 화면 "주재료"만 보여주려면 레시피 역할이 MAIN인 것만 필터
                // .where(recipe.recipeRole.eq(MenuRecipe.RecipeRole.MAIN))
                .orderBy(toOrderSpec(menu, sort))
                .transform(GroupBy.groupBy(menu.menuId).as(
                        Projections.fields(MenuListDTO.class,
                                menu.menuId.as("menuId"),
                                // NOTE: DTO의 타입과 맞춰주세요. (Boolean 권장)
                                menu.menuShow.as("menuShow"),
                                menu.menuName.as("menuName"),
                                category.menuCategoryName.as("menuCategoryName"),
                                menu.menuPrice.as("menuPrice"),
                                menu.menuKcal.as("menuKcal"),
                                GroupBy.list(material.name).as("materialNames")
                        )
                ));

        // 페이징 적용 (메모리 페이징이지만, 보통 조건 + 정렬이 동일하면 문제 없음)
        List<MenuListDTO> content = mapped.values().stream()
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .toList();

        // ===== total count (동일 where 조건) =====
        Long total = queryFactory
                .select(menu.menuId.countDistinct())
                .from(menu)
                .leftJoin(menu.menuCategory, category)
                .leftJoin(menu.recipe, recipe)
                .leftJoin(recipe.material, material)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    // =======================
    // 조건 빌더
    // =======================
    private BooleanExpression eqNameOrInfo(MenuSearchDTO dto, QMenu menu) {
        if (!StringUtils.hasText(dto.getKeyword())) return null;

        String type = (dto.getType() == null) ? "all" : dto.getType();
        String kw = dto.getKeyword();

        switch (type) {
            case "name":
                return menu.menuName.containsIgnoreCase(kw);
            case "info":
                return menu.menuInformation.containsIgnoreCase(kw);
            case "all":
            default:
                return menu.menuName.containsIgnoreCase(kw)
                        .or(menu.menuInformation.containsIgnoreCase(kw));
        }
    }

    private BooleanExpression eqCategory(MenuSearchDTO dto, QMenu menu) {
        if (dto.getMenuCategory() == null || dto.getMenuCategory() == MenuCategoryEnum.TOTAL) return null;
        return menu.menuCategory.menuCategoryName.eq(dto.getMenuCategory().getDescription());
    }

    // 여러 조건 and 결합
    private BooleanExpression andAll(BooleanExpression... exps) {
        BooleanExpression result = null;
        for (BooleanExpression exp : exps) {
            if (exp == null) continue;
            result = (result == null) ? exp : result.and(exp);
        }
        return result;
    }

    // 정렬 변환 (pageable Sort → QueryDSL OrderSpecifier[])
    private com.querydsl.core.types.OrderSpecifier<?>[] toOrderSpec(QMenu menu, Sort sort) {
        return sort.stream().map(order -> {
            com.querydsl.core.types.Order o =
                    order.getDirection().isAscending()
                            ? com.querydsl.core.types.Order.ASC
                            : com.querydsl.core.types.Order.DESC;

            // 지원하는 정렬 키만 매핑
            return switch (order.getProperty()) {
                case "menuId" -> new com.querydsl.core.types.OrderSpecifier<>(o, menu.menuId);
                case "menuName" -> new com.querydsl.core.types.OrderSpecifier<>(o, menu.menuName);
                case "menuPrice" -> new com.querydsl.core.types.OrderSpecifier<>(o, menu.menuPrice);
                case "menuKcal" -> new com.querydsl.core.types.OrderSpecifier<>(o, menu.menuKcal);
                default -> new com.querydsl.core.types.OrderSpecifier<>(com.querydsl.core.types.Order.DESC, menu.menuId);
            };
        }).toArray(com.querydsl.core.types.OrderSpecifier[]::new);
    }
}
