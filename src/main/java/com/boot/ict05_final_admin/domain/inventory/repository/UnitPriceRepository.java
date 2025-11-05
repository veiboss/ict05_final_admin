package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.UnitPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface UnitPriceRepository extends JpaRepository<UnitPrice, Long> {
    // 최신 매입 단가 조회(재료 ID <-> 가장 최근 단가 없으면 null)
    @Query("SELECT u.purchasePrice FROM UnitPrice u WHERE u.material.id = :materialId ORDER BY u.createdAt DESC LIMIT 1")
    BigDecimal findLatestPrice(@Param("materialId") Long materialId);
}
