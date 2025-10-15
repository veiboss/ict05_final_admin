package com.boot.ict05_final_admin.domain.menu.repository;

import com.boot.ict05_final_admin.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository  extends JpaRepository<Menu,Long>, MenuRepositoryCustom {
}
