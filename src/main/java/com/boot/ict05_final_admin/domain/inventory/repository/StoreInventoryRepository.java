package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.StoreInventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreInventoryRepository
        extends JpaRepository<StoreInventory, Long>, StoreInventoryRepositoryCustom {

}