package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 본사 입출고 통합 로그 Repository
 */
@Repository
public interface InventoryLogViewRepository extends JpaRepository<InventoryLogView, Long> {

    // 기본: 페이징 목록
    Page<InventoryLogView> findByMaterialId(Long materialId, Pageable pageable);

    // 확장: 필터 검색
    @Query(value = """
    SELECT * FROM v_inventory_log
    WHERE material_id = :materialId
      AND (:type IS NULL OR log_type = :type)
      AND (:start IS NULL OR log_date >= :start)
      AND (:end   IS NULL OR log_date <  :end)
    """,
            countQuery = """
    SELECT COUNT(*) FROM v_inventory_log
    WHERE material_id = :materialId
      AND (:type IS NULL OR log_type = :type)
      AND (:start IS NULL OR log_date >= :start)
      AND (:end   IS NULL OR log_date <  :end)
    """,
            nativeQuery = true)
    org.springframework.data.domain.Page<InventoryLogView> findLogsByFilter(
            @Param("materialId") Long materialId,
            @Param("type") String type,
            @Param("start") java.time.LocalDate start,
            @Param("end") java.time.LocalDate end,
            org.springframework.data.domain.Pageable pageable);
}
