package com.boot.ict05_final_admin.domain.menu.repository;

import com.boot.ict05_final_admin.domain.menu.dto.MenuListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MenuRepositoryCustom {
    Page<MenuListDTO> listMenu(Pageable pageable);
}
