package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryLotDetailDTO;
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
    private final InventoryBatchRepository inventoryBatchRepository;

    public List<InventoryBatch> getBatchesByMaterial(Long materialId) {
        return inventoryBatchRepository.findAllByMaterial_IdOrderByReceivedDateDesc(materialId);
    }

    /**
     * LOT(배치) 상세 조회
     *
     * @param batchId inventory_batch.id (또는 v_inventory_log 에서 넘어오는 LOT 기준 ID)
     */
    @Transactional(readOnly = true)
    public InventoryLotDetailDTO getLotDetail(Long batchId) {

        InventoryBatch b = inventoryBatchRepository.findById(batchId)
                .orElseThrow(() ->
                        new IllegalArgumentException("배치 정보를 찾을 수 없습니다. id=" + batchId));

        InventoryLotDetailDTO dto = new InventoryLotDetailDTO();
        dto.setBatchId(b.getId());
        dto.setMaterialId(b.getMaterial().getId());
        dto.setMaterialCode(b.getMaterial().getCode());
        dto.setMaterialName(b.getMaterial().getName());
        dto.setLotNo(b.getLotNo());
        dto.setReceivedDate(b.getReceivedDate());
        dto.setExpirationDate(b.getExpirationDate());
        dto.setReceivedQuantity(b.getReceivedQuantity());
        dto.setRemainingQuantity(b.getQuantity());
        dto.setUnitPrice(b.getUnitPrice());

        return dto;
    }


}
