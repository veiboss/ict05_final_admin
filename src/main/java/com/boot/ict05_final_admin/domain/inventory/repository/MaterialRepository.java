package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaterialRepository  extends JpaRepository<Material, Long>, MaterialRepositoryCustom {

    @Query("SELECT MAX(m.code) FROM Material m WHERE m.materialCategory = :category")
    String findMaxCodeByCategory(@Param("category") MaterialCategory category);
}
