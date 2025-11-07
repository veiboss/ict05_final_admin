package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryIn;
import java.time.LocalDateTime;
import java.util.List;

public interface InventoryInRepositoryCustom {
    List<InventoryIn> findRecentByMaterial(Long materialId, int limit);
    List<InventoryIn> findByMaterialAndPeriod(Long materialId, LocalDateTime from, LocalDateTime to);
}
