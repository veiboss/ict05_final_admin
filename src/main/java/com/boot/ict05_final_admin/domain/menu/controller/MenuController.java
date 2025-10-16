package com.boot.ict05_final_admin.domain.menu.controller;

import com.boot.ict05_final_admin.config.ProjectAttribute;
import com.boot.ict05_final_admin.domain.menu.dto.MenuListDTO;
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

@Controller
@RequiredArgsConstructor    // 생성자
public class MenuController {

    private final MenuService menuService;      // private final : 바꿀 수 없는 변수
    // private final ProjectAttribute projectAttribute;

    /** 목록 */
    @GetMapping("/menu/list")
    public String listStoreMenu( @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable, // 페이지 설정 id 기준
                                 Model model,
                                 HttpServletRequest request) {  // 사용자가 보낸 요청 정보를 담은 객체

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber()-1, pageable.getPageSize(), Sort.by("id").descending()); // 현재 페이지 번호와 한 페이지 개수를 기반으로 페이지 요청을 새로 생성
        Page<MenuListDTO> menu = menuService.selectMenu(pageRequest);   // DB에서 목록을 끌고와 DTO에 담아 페이지 객체

        model.addAttribute("menu", menu);   // html에 데이터 넘김
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request)); // 현재 요청 URL을 담음, html에 전달해서 페이지 이동 버튼 같은 거 만들 때 사용 가능

        return "menu/list";
    }


}
