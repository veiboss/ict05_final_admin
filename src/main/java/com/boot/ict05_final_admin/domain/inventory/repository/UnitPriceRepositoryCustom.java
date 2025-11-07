package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.UnitPrice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UnitPriceRepositoryCustom {
    Optional<UnitPrice> findLatestPurchasePrice(Long materialId, LocalDateTime at);
    List<UnitPrice> historyPurchasePrice(Long materialId, int limit);
}
