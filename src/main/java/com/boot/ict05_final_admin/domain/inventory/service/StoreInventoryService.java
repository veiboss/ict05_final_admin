package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.repository.StoreInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 가맹점 재고 서비스
 *
 * <p>조회 전용 (수정/삭제 없음)</p>
 */
@Service
@RequiredArgsConstructor
public class StoreInventoryService {

    private final StoreInventoryRepository storeInventoryRepository;

    /**
     * 가맹점 재고 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<StoreInventoryListDTO> listStoreInventories(StoreInventorySearchDTO searchDTO, Pageable pageable) {
        return storeInventoryRepository.listStoreInventory(searchDTO, pageable);
    }

    /**
     * 가맹점 재고 개수 조회
     */
    @Transactional(readOnly = true)
    public long countStoreInventory(StoreInventorySearchDTO searchDTO) {
        return storeInventoryRepository.countStoreInventory(searchDTO);
    }
}