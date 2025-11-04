package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 본사 재고 조정 Repository
 *
 * <p>재고 수량 조정 내역 insert 전용.
 * 조회는 InventoryLogViewRepository(뷰)에서 통합 처리한다.</p>
 */
@Repository
public interface InventoryAdjustmentRepository extends JpaRepository<InventoryAdjustment, Long> {
    // 특정 재료(material.id)의 마지막 재고조정 일시 조회 (마지막변동일 계산에 사용)
    @Query("select max(a.createdAt) from InventoryAdjustment a " +
            "where a.inventory.material.id = :materialId")
    LocalDateTime lastAdjAt(@Param("materialId") Long materialId);
}
