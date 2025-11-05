package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryBatchRepository  extends JpaRepository<InventoryBatch, Long> {
    // 일자별 시퀀스 조회 추가
    @Query("SELECT COUNT(b) FROM InventoryBatch b " +
            "WHERE b.material.id = :materialId AND DATE(b.createdAt) = CURRENT_DATE")
    Long countTodayByMaterial(@Param("materialId") Long materialId);
    
    // 사용가능한 배치 목록 조회( materialId <-> 수량이 남아 있는 배치 목록(입고순))
    @Query("SELECT b FROM InventoryBatch b WHERE b.material.id = :materialId AND b.quantity > 0 ORDER BY b.createdAt ASC")
    List<InventoryBatch> findAvailableBatches(@Param("materialId") Long materialId);
}
