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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryLogService {

    private final InventoryLogViewRepository inventoryLogViewRepository;
    private final StoreNameResolver storeNameResolver;

    /**
     * 본사 재고 로그 페이지 조회(DTO 반환)
     */
    public Page<InventoryLogDTO> getFilteredLogs(Long materialId,
                                                 String type,
                                                 LocalDate startDate,
                                                 LocalDate endDate,
                                                 Pageable pageable) {
        Page<InventoryLogView> page = inventoryLogViewRepository
                .findLogsByFilter(materialId, type, startDate, endDate, pageable);

        Set<Long> ids = page.getContent().stream()
                .map(InventoryLogView::getStoreId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long,String> nameMap = storeNameResolver.resolveAllWithFallback(ids);

        List<InventoryLogDTO> list = page.getContent().stream()
                .map(v -> InventoryLogDTO.builder()
                        .logId(v.getLogId())
                        .logDate(v.getDate())
                        .logType(v.getType())          // DTO는 String 타입
                        .quantity(v.getQuantity())
                        .stockAfter(v.getStockAfter())
                        .unitPrice(v.getUnitPrice())
                        .memo(v.getMemo())
                        .storeId(v.getStoreId())
                        .storeName(v.getStoreId()==null? null : nameMap.get(v.getStoreId()))
                        .build())
                .toList();

        return new PageImpl<>(list, pageable, page.getTotalElements());
    }
}
