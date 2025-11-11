package com.boot.ict05_final_admin.domain.inventory.repository;


import com.boot.ict05_final_admin.domain.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 본사 재고(Inventory) Repository
 *
 * <p>본사 재고의 수량 및 적정 수량 관리 기능을 담당한다.</p>
 */
public interface InventoryRepository
        extends JpaRepository<Inventory, Long>, InventoryRepositoryCustom {

    // 트랜잭션 내 동시 갱신 충돌을 막기 위해
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.material.id = :materialId")
    Optional<Inventory> findByMaterialIdForUpdate(@Param("materialId") Long materialId);

    boolean existsByMaterial_Id(Long materialId);

    Optional<Inventory> findByMaterialId(Long materialId);

}