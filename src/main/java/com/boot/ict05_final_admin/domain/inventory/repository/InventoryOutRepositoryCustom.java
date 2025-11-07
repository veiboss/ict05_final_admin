package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOut;

import java.math.BigDecimal;
import java.util.Optional;

public interface InventoryOutRepositoryCustom {

    BigDecimal getTotalQuantity(Long outId);

    /** 스토어까지 페치 조인 */
    Optional<InventoryOut> findByIdWithStore(Long id);
}
