package com.boot.ict05_final_admin.domain.menu.service;

import com.boot.ict05_final_admin.domain.menu.dto.MenuListDTO;
import com.boot.ict05_final_admin.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;

    /* 메뉴 목록 */
    public Page<MenuListDTO> selectMenu(Pageable  pageable) {
        return menuRepository.listMenu(pageable);
    }

}
