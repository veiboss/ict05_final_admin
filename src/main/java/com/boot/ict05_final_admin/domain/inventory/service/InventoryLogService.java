package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryLogViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryLogService {

    private final InventoryLogViewRepository inventoryLogViewRepository;

    /**
     * 본사 재고 로그 필터 조회 (페이징 포함)
     */
    public Page<InventoryLogView> getFilteredLogs(
            Long materialId,
            String type,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        return inventoryLogViewRepository.findLogsByFilter(
                materialId,
                type,
                startDate,
                endDate,
                pageable
        );
    }
}
