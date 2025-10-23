package com.boot.ict05_final_admin.domain.menu.repository;

import com.boot.ict05_final_admin.domain.menu.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    Optional<MenuCategory> findByMenuCategoryName(String menuCategoryName);
}
