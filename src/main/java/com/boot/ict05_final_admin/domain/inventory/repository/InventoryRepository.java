package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.HqInventory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 본사 재고 Repository.
 */
public interface InventoryRepository extends JpaRepository<HqInventory, Long>, InventoryRepositoryCustom {
}
