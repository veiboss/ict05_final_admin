package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MaterialRepository  extends JpaRepository<Material, Long>, MaterialRepositoryCustom {

    @Query("SELECT MAX(m.code) FROM Material m WHERE m.materialCategory = :category")
    String findMaxCodeByCategory(@Param("category") MaterialCategory category);


    /**
     * 카테고리별 재료 목록 조회
     * 본사 입고 등록 시, 선택된 카테고리에 속한 본사 사용 재료만 반환
     *
     * @param category 재료 카테고리 (예: BASE, SAUCE 등)
     * @return 카테고리 조건에 맞는 재료 목록
     */
    @Query("SELECT m FROM Material m WHERE m.materialCategory = :category AND m.materialStatus = com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus.USE")
    List<Material> findByCategory(@Param("category") MaterialCategory category);
}
