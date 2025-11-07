package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryAdjustment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface InventoryAdjustmentRepositoryCustom {

    /** 재고ID + 기간 조회 */
    List<InventoryAdjustment> findByInventoryAndPeriod(Long inventoryId, LocalDateTime from, LocalDateTime to);

    /** 재고ID + 기간 합계 */
    BigDecimal sumQuantityByInventoryAndPeriod(Long inventoryId, LocalDateTime from, LocalDateTime to);

    /** 재고ID 기준 마지막 조정 시각 */
    LocalDateTime lastAdjustmentAtByInventory(Long inventoryId);
}
