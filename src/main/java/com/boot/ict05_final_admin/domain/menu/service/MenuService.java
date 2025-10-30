package com.boot.ict05_final_admin.domain.menu.service;

import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.repository.MaterialRepository;
import com.boot.ict05_final_admin.domain.menu.dto.*;
import com.boot.ict05_final_admin.domain.menu.entity.Menu;
import com.boot.ict05_final_admin.domain.menu.entity.MenuCategory;
import com.boot.ict05_final_admin.domain.menu.entity.MenuRecipe;
import com.boot.ict05_final_admin.domain.menu.entity.MenuShow;
import com.boot.ict05_final_admin.domain.menu.repository.MenuCategoryRepository;
import com.boot.ict05_final_admin.domain.menu.repository.MenuRecipeRepository;
import com.boot.ict05_final_admin.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * 메뉴 관련 비즈니스 로직을 처리하는 서비스 클래스
 *
 * <p>메뉴 등록, 수정, 조회 등의 기능을 제공한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional      // DB 작업은 하나의 트랜잭션 단위로 처리 - 중간에 에러 나면 모두 취소
@Slf4j  // 로그를 찍을 수 있음
public class MenuService {

    private final MenuRepository menuRepository;    // @RequiredArgsConstructor가 자동으로 주입해 줘서 @Autowired가 필요 없음
    private final MaterialRepository materialRepository;
    private final MenuRecipeRepository menuRecipeRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    /**
     * 작성자 이름으로 필터링하여 메뉴 목록을 페이지 단위로 조회한다.
     *
     * @param menuSearchDTO
     * @param pageable      페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 메뉴 리스트 DTO
     */
    public Page<MenuListDTO> selectAllStoreMenu(MenuSearchDTO menuSearchDTO, Pageable pageable) {
        var menus = menuRepository.listMenu(menuSearchDTO, pageable);

        // 디버깅 로그 추가
        log.info("rows={}", menus.getNumberOfElements());
        menus.getContent().forEach(m ->
                log.info("id={}, name={}, materials={}", m.getMenuId(), m.getMenuName(), m.getMaterialNames())
        );

        return menus;
    }


    /**
     * 새로운 메뉴를 등록한다.
     *
     * @param dto 메뉴 등록 정보 (제목, 내용, 카테고리 등)
     * @return 저장된 메뉴 ID
     */
    @Transactional
    public Long insertStoreMenu(MenuWriteFormDTO dto) {
        MenuCategory category = menuCategoryRepository.findById(dto.getMenuCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음: " + dto.getMenuCategoryId()));

        MenuShow show = dto.getMenuShow() != null ? dto.getMenuShow() : MenuShow.HIDE;

        Menu menu = Menu.builder()
                .menuName(dto.getMenuName())
                .menuNameEnglish(dto.getMenuNameEnglish())
                .menuPrice(dto.getMenuPrice())
                .menuInformation(dto.getMenuInformation())
                .menuKcal(dto.getMenuKcal())
                .menuShow(show)
                .menuCategory(category)
                .build();

        menuRepository.save(menu);

        // 레시피 저장 (자유 입력: material FK 없음)
        saveRecipes(menu, dto.getMainMaterials(),  MenuRecipe.RecipeRole.MAIN);
        saveRecipes(menu, dto.getSauceMaterials(), MenuRecipe.RecipeRole.SAUCE);

        return menu.getMenuId();
    }


    /**
     * ID를 기준으로 메뉴를 조회한다.
     *
     * @param menuId 메뉴 ID
     * @return 메뉴 엔티티, 존재하지 않으면 null
     */
    public Menu findMenuById(Long menuId) {
        return menuRepository.findById(menuId).orElse(null);
    }

    /**
     * 기존 메뉴 수정한다
     *
     * @param dto   수정할 메뉴 정보
     * @return 수정된 메뉴 엔티티
     */
    @Transactional
    public Menu menuModify(MenuModifyFormDTO dto) {
        // 수정 대상 메뉴 조회
        Menu menu = findMenuById(dto.getMenuId());
        if (menu == null) {
            throw new IllegalArgumentException("메뉴가 존재하지 않습니다.");
        }

        // 엔티티 내부 update 메서드 호출 (DTO 내용으로 갱신)
        menu.updateMenu(dto);

        // 3) 카테고리 변경이 필요하다면 갱신
        if (dto.getMenuCategoryName() != null) {
            MenuCategory category = menuCategoryRepository.findByMenuCategoryName(dto.getMenuCategoryName())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리 없음: " + dto.getMenuCategoryName()));
            menu.setMenuCategory(category);
        }

        // 레시피 수정 (필요 시 기존 레시피 제거 후 재등록)
        if (dto.getMainMaterials() != null || dto.getSauceMaterials() != null) {
            menuRecipeRepository.deleteAllByMenu(menu); // 기존 레시피 삭제

            saveRecipes(menu, dto.getMainMaterials(),  MenuRecipe.RecipeRole.MAIN);
            saveRecipes(menu, dto.getSauceMaterials(), MenuRecipe.RecipeRole.SAUCE);

        }

        return menu;
    }

    /**
     * 메뉴 상세 정보를 조회한다.
     *
     * @param menuId 공지사항 ID
     * @return 메뉴 엔티티, 존재하지 않으면 null
     */
    public MenuDetailDTO MenuDetail(Long menuId) {
        Menu m = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("menu not found: " + menuId));

        // 주재료
        List<RecipeItemDTO> mains = m.getRecipe().stream()
                .filter(r -> r.getRecipeRole() == MenuRecipe.RecipeRole.MAIN)
                .sorted(Comparator.comparing(MenuRecipe::getRecipeSort))
                .map(r -> {
                    RecipeItemDTO d = new RecipeItemDTO();
                    d.setMaterialId(r.getMaterial() != null ? r.getMaterial().getId() : null);
                    d.setItemName(r.getRecipeItemName());           // null이면 템플릿에서 materialName 표시
                    d.setRecipeQty(r.getRecipeQty());
                    d.setRecipeUnit(r.getRecipeUnit());
                    d.setRecipeSortNo(r.getRecipeSort());
                    // (선택) 화면 편의용 materialName 필드가 필요하면 DTO에 추가해서 세팅
                    return d;
                }).toList();

        // 소스
        List<RecipeItemDTO> sauces = m.getRecipe().stream()
                .filter(r -> r.getRecipeRole() == MenuRecipe.RecipeRole.SAUCE)
                .sorted(Comparator.comparing(MenuRecipe::getRecipeSort))
                .map(r -> {
                    RecipeItemDTO d = new RecipeItemDTO();
                    d.setMaterialId(r.getMaterial() != null ? r.getMaterial().getId() : null);
                    d.setItemName(r.getRecipeItemName());
                    d.setRecipeQty(r.getRecipeQty());
                    d.setRecipeUnit(r.getRecipeUnit());
                    d.setRecipeSortNo(r.getRecipeSort());
                    return d;
                }).toList();

        return MenuDetailDTO.builder()
                .menuId(m.getMenuId())
                .menuCategoryId(m.getMenuCategory().getMenuCategoryId())
                .menuCategory(m.getMenuCategory())
                .menuShow(m.getMenuShow())
                .menuCode(m.getMenuCode())
                .menuName(m.getMenuName())
                .menuNameEnglish(m.getMenuNameEnglish())
                .menuPrice(m.getMenuPrice())
                .menuInformation(m.getMenuInformation())
                .menuKcal(m.getMenuKcal())
                .mainMaterials(mains)
                .sauceMaterials(sauces)
                .build();
    }


    /**
     * 자유입력 레시피 저장 유틸 (옵션 A)
     * - material FK 사용 안 함
     * - recipeItemName / recipeQty / recipeUnit / recipeRole / recipeSort 만 저장
     */
    private void saveRecipes(Menu menu,
                             List<RecipeItemDTO> items,
                             MenuRecipe.RecipeRole role) {

        if (items == null || items.isEmpty()) return;

        int sort = 1;
        for (RecipeItemDTO it : items) {
            if (it == null) continue;

            // 수량/단위 필수 값 체크
            if (it.getRecipeQty() == null || it.getRecipeQty().signum() <= 0) continue;
            if (it.getRecipeUnit() == null) continue;

            Material material = null;
            if (it.getMaterialId() != null) {
                material = materialRepository.findById(it.getMaterialId())
                        .orElse(null); // 못 찾으면 그냥 null 처리
            }

            // ✅ itemName 보정: 비어있으면 재료명 또는 "기타"
            String itemName = it.getItemName();
            if (itemName == null || itemName.isBlank()) {
                itemName = (material != null ? material.getName() : "기타"); // DB가 NOT NULL이면 필수
            }

            MenuRecipe recipe = MenuRecipe.builder()
                    .menu(menu)
                    .material(material)  // ✅ null 가능
                    .recipeItemName(itemName)
                    .recipeQty(it.getRecipeQty())
                    .recipeUnit(it.getRecipeUnit())
                    .recipeSort(sort++)
                    .recipeRole(role)
                    .build();

            menuRecipeRepository.save(recipe);
        }
    }



    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

}
