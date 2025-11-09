package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.BatchStatusRowDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.FifoCandidateDTO;

import java.util.List;

public interface InventoryBatchQueryRepository {
    List<BatchStatusRowDTO> findBatchStatusByMaterial(Long materialId);
    List<FifoCandidateDTO> findAvailableBatchesForFifo(Long materialId);
}
