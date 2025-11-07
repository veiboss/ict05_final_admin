package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.repository.InventoryAdjustmentRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryInRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutLotRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 기본 재고 서비스 (InventoryService)
 *
 * <p>간단한 오버뷰/합계 등 서비스 파사드.</p>
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryBatchRepository batchRepo;
    private final InventoryOutLotRepository outLotRepo;
    private final InventoryInRepository inRepo;
    private final InventoryAdjustmentRepository adjustRepo;

    /**
     * HQ 현재고(재료ID 기준)
     *
     * <p>HQ 배치 잔량 합.</p>
     */
    @Comment("HQ 현재고 합계")
    public BigDecimal hqRemainOfMaterial(Long materialId) {
        return batchRepo.findHqBatchesForMaterial(materialId).stream()
                .map(b -> b.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 기존 컨트롤러 시그니처 호환용 어댑터가 필요하면 여기에 추가한다.
}
