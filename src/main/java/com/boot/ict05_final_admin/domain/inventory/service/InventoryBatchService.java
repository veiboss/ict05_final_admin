package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryBatchService {
    private final InventoryBatchRepository batchRepo;

    public List<InventoryBatch> getBatchesByMaterial(Long materialId) {
        return batchRepo.findAllByMaterial_IdOrderByReceivedDateDesc(materialId);
    }
}
