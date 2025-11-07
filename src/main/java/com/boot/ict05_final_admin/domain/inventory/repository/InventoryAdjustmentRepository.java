package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryAdjustmentRepository
        extends JpaRepository<InventoryAdjustment, Long>, InventoryAdjustmentRepositoryCustom {
}
