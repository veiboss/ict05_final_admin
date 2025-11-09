package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryLogViewRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 로그 뷰 서비스 (InventoryLogViewService)
 *
 * <p>화면용 집계/뷰 테이블 페이징을 제공한다.</p>
 */
@Service
@RequiredArgsConstructor
public class InventoryLogViewService {

    private final InventoryLogViewRepository repo;

    /**
     * 재료ID + 기간 페이징 조회
     */
    @Transactional(readOnly = true)
    public Page<InventoryLogView> getFilteredLogs(Long materialId,
                                                  String type,
                                                  LocalDate startDate,
                                                  LocalDate endDate,
                                                  Pageable pageable) {
        return repo.findLogsByFilter(materialId, type, startDate, endDate, pageable);
    }
}
