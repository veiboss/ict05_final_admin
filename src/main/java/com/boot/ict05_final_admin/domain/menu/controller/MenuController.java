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
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final ProjectAttribute projectAttribute;

    /* 목록 */
    @GetMapping("/menu/list")
    public String listStoreMenu( @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                 Model model,
                                 HttpServletRequest request) {

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber()-1, pageable.getPageSize(), Sort.by("id").descending());
        Page<MenuListDTO> menu = menuService.selectMenu(pageRequest);

        model.addAttribute("menu", menu);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));

        return "menu/list";
    }


}
