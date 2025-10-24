package com.boot.ict05_final_admin.domain.menu.controller;

import com.boot.ict05_final_admin.config.ProjectAttribute;
import com.boot.ict05_final_admin.domain.menu.dto.MenuListDTO;
import com.boot.ict05_final_admin.domain.menu.dto.MenuSearchDTO;
import com.boot.ict05_final_admin.domain.menu.dto.MenuWriteFormDTO;
import com.boot.ict05_final_admin.domain.menu.entity.Menu;
import com.boot.ict05_final_admin.domain.menu.entity.MenuCategory;
import com.boot.ict05_final_admin.domain.menu.repository.MenuCategoryRepository;
import com.boot.ict05_final_admin.domain.menu.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * 본사 메뉴 관리 컨트롤러
 * <p>
 * 메뉴 작성, 메뉴 목록 조회, 상세 조회, 수정 화면을 제공한다.
 * 카테고리, 첨부파일 기능을 지원한다.
 */
@Controller
@RequiredArgsConstructor    // 생성자
public class MenuController {

    private final MenuService menuService;      // private final : 바꿀 수 없는 변수
    private final ProjectAttribute projectAttribute;
    private final MenuCategoryRepository menuCategoryRepository;

    /**
     * 메뉴 목록을 페이징 처리하여 조회한다.
     *
     * @param menuSearchDTO 메뉴 이름으로 검색할 경우 전달되는 값
     * @param pageable 페이지 번호, 크기, 정렬 조건을 포함한 페이징 객체
     * @param model    뷰에 전달할 모델 객체
     * @return 메뉴 목록 페이지 뷰 이름
     */
    @GetMapping("/menu/list")
    public String listStoreMenu(
            MenuSearchDTO menuSearchDTO,
            @PageableDefault(page = 0, size = 10, sort = "menuId", direction = Sort.Direction.DESC) Pageable pageable,
            Model model,
            HttpServletRequest request) {

        int size = resolveSize(menuSearchDTO.getSize(), pageable.getPageSize());
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "menuId");

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), size, sort);

        Page<MenuListDTO> menus = menuService.selectAllStoreMenu(menuSearchDTO, pageRequest);

        // ✅ 카테고리: 리프(레벨3)만 + "세트메뉴" 하나 추가
        List<MenuCategory> categories = new ArrayList<>(
                menuCategoryRepository.findAllByMenuCategoryLevel(3, Sort.by("menuCategoryName").ascending())
        );
        List<MenuCategory> finalCategories = categories;
        menuCategoryRepository.findByMenuCategoryName("세트메뉴")
                .ifPresent(c -> finalCategories.add(0, c));

        // 혹시라도 중복 방지 (같은 ID가 들어갈 가능성 대비)
        categories = categories.stream()
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toMap(
                                MenuCategory::getMenuCategoryId,
                                c -> c,
                                (a, b) -> a,
                                LinkedHashMap::new
                        ),
                        m -> new ArrayList<>(m.values())
                ));

        model.addAttribute("menus", menus);
        model.addAttribute("menuSearchDTO", menuSearchDTO);
        model.addAttribute("menuCategories", categories);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));

        return "menu/list";
    }



    // util
    private int resolveSize(String s, int fallback) {
        try {
            if (s == null || s.isBlank()) return fallback;
            int v = Integer.parseInt(s);
            return (v < 1) ? fallback : v;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * 메뉴 작성 화면을 표시한다.
     *
     * @param model 뷰에 전달할 모델 객체
     * @return 메뉴 작성 페이지 뷰 이름
     */
    @GetMapping("/menu/write")
    public String addStoreMenu(Model model) {
        List<MenuCategory> categories = menuCategoryRepository.findAll(Sort.by("menuCategoryName").ascending());

        model.addAttribute("menuWriteFormDTO", new MenuWriteFormDTO());
        model.addAttribute("menuCategories", categories);

        return "menu/write";
    }

    /**
     * 특정 메뉴 상세 내용을 조회한다.
     *
     * @param menuId    메뉴 ID
     * @param model 뷰에 전달할 모델 객체
     * @return 메뉴 상세 페이지 뷰 이름
     */
    @GetMapping("/menu/detail/{menuId}")
    public String detailStoreMenu(@PathVariable Long menuId, Model model) {
        Menu menu = menuService.detailMenu(menuId);

        model.addAttribute("menu", menu);

        return "menu/detail";
    }

    /**
     * 특정 메뉴의 수정 화면을 표시한다.
     *
     * @param menuId    메뉴 ID
     * @param model 뷰에 전달할 모델 객체
     * @return 메뉴 수정 페이지 뷰 이름
     */
    @GetMapping("/menu/modify/{menuId}")
    public String modifyStoreMenu(@PathVariable Long menuId, Model model) {
        Menu menu = menuService.detailMenu(menuId);

        List<MenuCategory> categories = menuCategoryRepository.findAll(Sort.by("menuCategoryName").ascending());

        model.addAttribute("menu", menu);
        model.addAttribute("menuCategories", categories);

        return "menu/modify";
    }


}
