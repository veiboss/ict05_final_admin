package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 본사 재고 서비스
 *
 * <p>재고 목록 조회를 담당한다.</p>
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * 재고 목록을 페이지 단위로 조회한다.
     *
     * @param inventorySearchDTO 검색 조건 DTO
     * @param pageable 페이지 정보
     * @return 페이징 처리된 재료 리스트
     */
    @Transactional(readOnly = true)
    public Page<InventoryListDTO> getInventoryList(InventorySearchDTO inventorySearchDTO, Pageable pageable) {
        return inventoryRepository.listInventory(inventorySearchDTO, pageable);
    }
}
