package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface InventoryLogViewRepositoryCustom {

    Page<InventoryLogView> pageByMaterialAndPeriod(Long materialId,
                                                   LocalDateTime from,
                                                   LocalDateTime to,
                                                   Pageable pageable);

    Page<InventoryLogView> findLogsByFilter(Long materialId,
                                            String type,          // null이면 전체
                                            LocalDate startDate,  // null이면 제한 없음
                                            LocalDate endDate,    // null이면 제한 없음
                                            Pageable pageable);
}
