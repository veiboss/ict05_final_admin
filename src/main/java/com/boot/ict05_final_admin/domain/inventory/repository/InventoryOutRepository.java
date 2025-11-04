package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface InventoryOutRepository extends JpaRepository<InventoryOut, Long> {
    // 기간 출고수량 합계. 일평균사용량 계산의 분자. DOH(커버리지) 계산에 쓰임
    @Query("select coalesce(sum(o.quantity),0) from InventoryOut o " +
            "where o.material.id=:materialId and o.createdAt >= :from and o.createdAt < :toExclusive")
    BigDecimal sumOutQty(@Param("materialId") Long materialId,
                         @Param("from") LocalDateTime from,
                         @Param("toExclusive") LocalDateTime toExclusive);
    // 마지막 출고일시
    @Query("select max(o.createdAt) from InventoryOut o where o.material.id=:materialId")
    LocalDateTime lastOutAt(@Param("materialId") Long materialId);
}