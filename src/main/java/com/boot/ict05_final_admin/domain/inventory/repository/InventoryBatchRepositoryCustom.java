package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryBatchRepositoryCustom {

    /** HQ 배치: 재료별, 잔량>0, 유통기한↑→입고일↑, 만료 NULL 후순위 */
    List<InventoryBatch> findHqBatchesForMaterial(Long materialId);

    /** FIFO 후보: 재료별, 잔량>0, 유통기한↑→입고일↑ */
    List<InventoryBatch> findFifoCandidates(Long materialId);

    /** 잔량 원자 차감. 성공 시 1, 실패 시 0 */
    int decrementQuantity(Long batchId, BigDecimal delta);

    /** 로트번호 생성시 사용 */
    long countByMaterialAndDate(Long materialId, java.time.LocalDate targetDate);

}
