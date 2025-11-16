package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryLogDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryLogViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 로그 뷰 서비스 (InventoryLogViewService)
 *
 * <p>화면용 집계/뷰 테이블 페이징을 제공한다.</p>
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryLogViewService {

    private final InventoryLogViewRepository inventoryLogViewRepository;

    /**
     * 재료ID + 기간 페이징 조회
     */
    @Transactional(readOnly = true)
    public Page<InventoryLogDTO> getFilteredLogs(Long materialId,
                                                 String type,
                                                 LocalDate startDate,
                                                 LocalDate endDate,
                                                 Pageable pageable) {

        Page<InventoryLogView> page = inventoryLogViewRepository
                .findLogsByFilter(materialId, type, startDate, endDate, pageable);

        List<InventoryLogDTO> dtoList = page.getContent().stream()
                .map(this::toDto)
                .toList();

        // 디버그용 로그
        dtoList.forEach(d ->
                log.info("LOG DTO => id={}, type={}, qty={}",
                        d.getLogId(), d.getLogType(), d.getQuantity())
        );

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    /**
     * v_inventory_log 엔티티 → 화면/엑셀 공용 DTO.
     * logType 은 절대 재계산하지 않고 뷰 값을 그대로 쓴다.
     */
    private InventoryLogDTO toDto(InventoryLogView row) {
        return InventoryLogDTO.builder()
                .logId(row.getRowId())
                .logDate(row.getDate())
                .logType(row.getType())
                .quantity(row.getQuantity())
                .stockAfter(row.getStockAfter())
                .unitPrice(row.getUnitPrice())
                .memo(row.getMemo())
                .storeId(row.getStoreId())
                .storeName(row.getStoreName())
                .batchId(row.getBatchId())
                .build();
    }
}
