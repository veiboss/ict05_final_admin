package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 재고 목록 커스텀 조회용 Repository 인터페이스.
 */
public interface InventoryRepositoryCustom {
    Page<InventoryListDTO> listInventory(InventorySearchDTO searchDTO, Pageable pageable);
    long countInventory(InventorySearchDTO searchDTO);


}
