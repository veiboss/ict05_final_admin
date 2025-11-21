package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 본사 재고 목록 커스텀 조회.
 */
public interface InventoryRepositoryCustom {

    /**
     * 검색 DTO 기반 페이지 조회.
     */
    Page<InventoryListDTO> listInventory(InventorySearchDTO searchDTO, Pageable pageable);

    /**
     * 검색 DTO 기반 총건수.
     */
    long countInventory(InventorySearchDTO searchDTO);
}
