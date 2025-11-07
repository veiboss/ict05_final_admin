package com.boot.ict05_final_admin.domain.nav.controller;

import com.boot.ict05_final_admin.domain.nav.dto.NavListDTO;
import com.boot.ict05_final_admin.domain.nav.dto.NavSearchDTO;
import com.boot.ict05_final_admin.domain.nav.service.NavGateService;
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
 * 시스템 메뉴 관리 화면 컨트롤러.
 *
 * 시스템 메뉴 조회 등
 * 화면 렌더링과 모델 구성 역할을 담당한다.
 */
@Controller
@RequiredArgsConstructor
public class NavauthController {

    private final NavGateService navGateService;

    @GetMapping("/navauth/list")
    public String listOfficeNavauth(NavSearchDTO navSearchDTO,
                                    @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                    Model model,
                                    HttpServletRequest request) {
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber()-1,
                pageable.getPageSize(),
                Sort.by("id").descending());

        Page<NavListDTO> navs = navGateService.selectAllNav(navSearchDTO, pageRequest);

        model.addAttribute("navs", navs);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("navSearchDTO", navSearchDTO);

        return "navauth/list";
    }
}
