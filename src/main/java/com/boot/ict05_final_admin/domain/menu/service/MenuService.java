package com.boot.ict05_final_admin.domain.menu.service;

import com.boot.ict05_final_admin.domain.inventory.repository.MaterialRepository;
import com.boot.ict05_final_admin.domain.menu.dto.MenuListDTO;
import com.boot.ict05_final_admin.domain.menu.dto.MenuModifyFormDTO;
import com.boot.ict05_final_admin.domain.menu.dto.MenuSearchDTO;
import com.boot.ict05_final_admin.domain.menu.dto.MenuWriteFormDTO;
import com.boot.ict05_final_admin.domain.menu.entity.Menu;
import com.boot.ict05_final_admin.domain.menu.entity.MenuRecipe;
import com.boot.ict05_final_admin.domain.menu.entity.MenuShowEnum;
import com.boot.ict05_final_admin.domain.menu.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuRecipeRepository menuRecipeRepository;
    private final MenuAllergyRepository menuAllergyRepository;
    private final AllergyRepository allergyRepository;

    /**
     * 작성자 이름으로 필터링하여 메뉴 목록을 페이지 단위로 조회한다.
     *
     * @param menuSearchDTO
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 메뉴 리스트 DTO
     */
    public Page<MenuListDTO> selectAllStoreMenu(MenuSearchDTO menuSearchDTO, Pageable pageable) {
        return menuRepository.listMenu(menuSearchDTO, pageable);
    }

    /**
     * 새로운 메뉴를 등록하고 첨부파일을 저장한다.
     *
     * @param dto  메뉴 등록 정보 (제목, 내용, 카테고리 등)
     * @return 저장된 메뉴 ID
     */
    @Transactional
    public Long insertStoreMenu(MenuWriteFormDTO dto) {

        /**
         * 새로운 메뉴 등록한다.
         *
         * @param dto   메뉴 등록 정보 (메뉴명, 내용, 카테고리 등)
         * @return 저장된 메뉴 ID
         */
        // 메뉴 기본정보 엔티티 생성
        Menu menu = Menu.builder()
                .menuName(dto.getMenuName())
                .menuNameEnglish(dto.getMenuNameEnglish())
                .menuPrice(dto.getMenuPrice())
                .menuInformation(dto.getMenuInformation())
                .menuKcal(dto.getMenuKcal())
                .menuShow(dto.getMenuShow() == MenuShowEnum.SHOW)
                .menuCategory(menuCategoryRepository.findByMenuCategoryName(dto.getMenuCategoryName())
                        .orElseThrow(() -> new IllegalArgumentException("카테고리 없음: " + dto.getMenuCategoryName())))
                .build();

        // DB 저장 (PK 생성)
        menuRepository.save(menu);

        // 주재료(레시피) 저장
        if (dto.getMainMaterials() != null) {
            dto.getMainMaterials().forEach(item -> {
                MenuRecipe recipe = MenuRecipe.builder()
                        .menu(menu)
                        .material(materialRepository.findById(item.getMaterialId())
                                .orElseThrow(() -> new IllegalArgumentException("재료 없음: " + item.getMaterialId())))
                        .recipeQty(new BigDecimal(item.getQty()))
                        .recipeUnit(item.getUnit())
                        .recipeRole(RecipeRole.MAIN)
                        .recipeSort(item.getSortNo())
                        .build();
                menuRecipeRepository.save(recipe);
            });
        }

        // 소스(레시피) 저장
        if (dto.getSauceMaterials() != null) {
            dto.getSauceMaterials().forEach(item -> {
                MenuRecipe recipe = MenuRecipe.builder()
                        .menu(menu)
                        .material(materialRepository.findById(item.getMaterialId())
                                .orElseThrow(() -> new IllegalArgumentException("재료 없음: " + item.getMaterialId())))
                        .recipeQty(new BigDecimal(item.getQty())
                        .recipeUnit(item.getUnit())
                        .recipeRole(RecipeRole.SAUCE)
                        .recipeSort(item.getSortNo())
                        .build();
                menuRecipeRepository.save(recipe);
            });
        }

        // 알레르기(선택된 항목) 연결
        if (dto.getAllergyIds() != null && !dto.getAllergyIds().isEmpty()) {
            dto.getAllergyIds().forEach(allergyId -> {
                MenuAllergy menuAllergy = MenuAllergy.builder()
                        .menu(menu)
                        .allergy(allergyRepository.findById(allergyId)
                                .orElseThrow(() -> new IllegalArgumentException("알레르기 없음: " + allergyId)))
                        .build();
                menuAllergyRepository.save(menuAllergy);
            });
        }

        return menu.getMenuId();
    }






    public Menu menuModify(MenuModifyFormDTO dto)


}
