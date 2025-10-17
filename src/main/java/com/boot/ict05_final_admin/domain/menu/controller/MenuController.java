package com.boot.ict05_final_admin.domain.menu.controller;

import com.boot.ict05_final_admin.config.ProjectAttribute;
import com.boot.ict05_final_admin.domain.menu.dto.MenuListDTO;
import com.boot.ict05_final_admin.domain.menu.dto.MenuSearchDTO;
import com.boot.ict05_final_admin.domain.menu.dto.MenuWriteFormDTO;
import com.boot.ict05_final_admin.domain.menu.entity.MenuCategory;
import com.boot.ict05_final_admin.domain.menu.service.MenuAttachmentService;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


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
    private final MenuAttachmentService menuAttachmentService;
    private final ProjectAttribute projectAttribute;

    /**
     * 메뉴 목록을 페이징 처리하여 조회한다.
     *
     * @param menuSearchDTO 메뉴 이름으로 검색할 경우 전달되는 값
     * @param pageable 페이지 번호, 크기, 정렬 조건을 포함한 페이징 객체
     * @param model    뷰에 전달할 모델 객체
     * @return 메뉴 목록 페이지 뷰 이름
     */
    @GetMapping("/menu/list")
    public String listStoreMenu(MenuSearchDTO menuSearchDTO,
                                @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable, // 페이지 설정 id 기준
                                Model model,
                                HttpServletRequest request) {  // 사용자가 보낸 요청 정보를 담은 객체

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber()-1, pageable.getPageSize(), Sort.by("id").descending()); // 현재 페이지 번호와 한 페이지 개수를 기반으로 페이지 요청을 새로 생성
        Page<MenuListDTO> menu = menuService.selectMenu(menuSearchDTO, pageRequest);   // DB에서 목록을 끌고와 DTO에 담아 페이지 객체

        model.addAttribute("menu", menu);   // html에 데이터 넘김
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request)); // 현재 요청 URL을 담음, html에 전달해서 페이지 이동 버튼 같은 거 만들 때 사용 가능
        model.addAttribute("menuSearchDTO", menuSearchDTO);

        return "menu/list";
    }

    /**
     * 메뉴 작성 화면을 표시한다.
     *
     * @param model 뷰에 전달할 모델 객체
     * @return 메뉴 작성 페이지 뷰 이름
     */
    @GetMapping("/menu/write")
    public String addStoreMenu(Model model) {
        model.addAttribute("menuWriteFormDTO" , new MenuWriteFormDTO()); // 폼 입력용 DTO를 초기화, 즉 비어있는 객체 새로 생성
        model.addAttribute("MenuCategory", MenuCategory.values());

        return "menu/write";
    }


}
