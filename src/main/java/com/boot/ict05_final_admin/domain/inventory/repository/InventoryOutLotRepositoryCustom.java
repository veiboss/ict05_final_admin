package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOutLot;
import com.querydsl.core.Tuple;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryOutLotRepositoryCustom {

    /** 출고ID로 아이템 목록 */
    List<InventoryOutLot> findByOutId(Long outId);

    /** 배치ID 기준, 출고/스토어 같이 로딩 */
    List<InventoryOutLot> findAllByBatchIdWithOutAndStore(Long batchId);

    /** 재료별 로트×가맹점 유출 집계: (batchId, storeId, sumQty) */
    List<Tuple> sumByBatchAndStoreForMaterial(Long materialId);

    boolean existsByOutIdAndBatchId(Long outId, Long batchId);

    BigDecimal sumQuantityByOutId(Long outId);
}
