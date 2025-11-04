package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface InventoryInRepository extends JpaRepository<InventoryIn, Long> {
    // 누적 입고금액 합계(단가×수량). 이동평균단가 계산의 분자.
    @Query("SELECT COALESCE(SUM(i.unitPrice * i.quantity), 0) FROM InventoryIn i WHERE i.material.id = :materialId")
    BigDecimal sumInAmount(@Param("materialId") Long materialId);

    // 누적 입고수량 합계. 이동평균단가 계산의 분모.
    @Query("select coalesce(sum(i.quantity),0) from InventoryIn i where i.material.id=:materialId")
    BigDecimal sumInQty(@Param("materialId") Long materialId);

    // 마지막 입고일시. 마지막변동일 계산 시 후보 값.
    @Query("select max(i.createdAt) from InventoryIn i where i.material.id=:materialId")
    LocalDateTime lastInAt(@Param("materialId") Long materialId);
}
