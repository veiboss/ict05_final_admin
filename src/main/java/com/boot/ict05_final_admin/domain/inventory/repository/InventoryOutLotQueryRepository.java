package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.BatchOutRowDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryOutLotQueryRepository {
    Page<BatchOutRowDTO> pageOutHistoryByBatch(Long batchId, Pageable pageable);
    void deleteOutById(Long lotId);
}
