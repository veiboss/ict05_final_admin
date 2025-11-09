package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.BatchOutRowDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.BatchStatusRowDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.OutLotHistoryRowDTO;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchQueryRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutLotQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 로트(배치) 조회 서비스
 *
 * <p>조회는 전부 QueryRepository로 위임한다.</p>
 */
@Service
@RequiredArgsConstructor
public class InventoryLotService {

    private final InventoryBatchQueryRepository batchRepo;
    private final InventoryOutLotQueryRepository outLotRepo;

    /**
     * 재료별 배치 현황을 조회한다.
     *
     * @param materialId 재료 ID
     * @return 배치 현황 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<BatchStatusRowDTO> getBatchStatusForMaterial(Long materialId) {
        return batchRepo.findBatchStatusByMaterial(materialId);
    }

    /**
     * 특정 배치의 출고 이력을 페이징 조회한다.
     *
     * @param batchId  배치 ID
     * @param pageable 페이지 요청
     * @return 출고 이력 DTO 페이지
     */
    @Transactional(readOnly = true)
    public Page<OutLotHistoryRowDTO> getOutLotHistory(Long batchId, Pageable pageable) {
        Page<BatchOutRowDTO> rows = outLotRepo.pageOutHistoryByBatch(batchId, pageable);

        List<OutLotHistoryRowDTO> mapped = rows.getContent().stream()
                .map(r -> OutLotHistoryRowDTO.builder()
                        .outId(r.getOutId())
                        .storeId(r.getStoreId())
                        .storeName(r.getStoreName())
                        .qty(r.getQty())
                        .outDate(r.getOutDate())
                        .build())
                .toList();

        return new PageImpl<>(mapped, pageable, rows.getTotalElements());
    }

    /**
     * 출고-로트 아이템을 삭제한다.
     *
     * @param lotId 출고-로트 아이템 ID
     */
    @Transactional
    public void deleteOutLot(Long lotId) {
        outLotRepo.deleteOutById(lotId);
    }
}
