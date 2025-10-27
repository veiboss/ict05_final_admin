package com.boot.ict05_final_admin.domain.inventory.repository;


import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventorySearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreInventoryRepositoryCustom {
    Page<StoreInventoryListDTO> listStoreInventory(StoreInventorySearchDTO searchDTO, Pageable pageable);
    long countStoreInventory(StoreInventorySearchDTO searchDTO);
}
