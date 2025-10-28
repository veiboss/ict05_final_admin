package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryIn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryInRepository extends JpaRepository<InventoryIn, Long> {
}
