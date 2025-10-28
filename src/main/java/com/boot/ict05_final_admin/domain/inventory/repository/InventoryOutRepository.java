package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOut;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryOutRepository extends JpaRepository<InventoryOut, Long> {
}