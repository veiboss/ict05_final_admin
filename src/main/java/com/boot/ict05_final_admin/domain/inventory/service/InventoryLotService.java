package com.boot.ict05_final_admin.domain.inventory.service;


import com.boot.ict05_final_admin.common.error.BusinessException;
import com.boot.ict05_final_admin.domain.inventory.dto.BatchOutRowDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.BatchStatusRowDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventoryOutLotDetailRowDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.InventoryOutLotHistoryRowDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOut;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOutLot;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryRecordStatus;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchQueryRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutLotQueryRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutLotRepository;

import jakarta.persistence.EntityNotFoundException;
import com.boot.ict05_final_admin.common.error.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.boot.ict05_final_admin.domain.inventory.utility.InventoryLogIdUtil.unwrap;

import java.util.List;

/**
 * 로트(배치) 조회 서비스
 *
 * <p>조회는 전부 QueryRepository로 위임한다.</p>
 */
@Service
@RequiredArgsConstructor
public class InventoryLotService {

    private final InventoryBatchQueryRepository inventoryBatchQueryRepository;
    private final InventoryOutLotQueryRepository inventoryOutLotQueryRepository;
    private final InventoryOutLotRepository inventoryOutLotRepository;


    /**
     * 재료별 배치 현황을 조회한다.
     *
     * @param materialId 재료 ID
     * @return 배치 현황 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<BatchStatusRowDTO> getBatchStatusForMaterial(Long materialId) {
        return inventoryBatchQueryRepository.findBatchStatusByMaterial(materialId);
    }

    /**
     * 특정 배치의 출고 이력을 페이징 조회한다.
     *
     * @param batchId  배치 ID
     * @param pageable 페이지 요청
     * @return 출고 이력 DTO 페이지
     */
    @Transactional(readOnly = true)
    public Page<InventoryOutLotHistoryRowDTO> getOutLotHistory(Long batchId, Pageable pageable) {
        Page<BatchOutRowDTO> rows = inventoryOutLotQueryRepository.pageOutHistoryByBatch(batchId, pageable);

        List<InventoryOutLotHistoryRowDTO> mapped = rows.getContent().stream()
                .map(r -> InventoryOutLotHistoryRowDTO.builder()
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
        InventoryOutLot lot = inventoryOutLotRepository.findById(lotId)
                .orElseThrow(() -> new EntityNotFoundException("출고 LOT가 존재하지 않습니다. id=" + lotId));

        InventoryOut header = lot.getOut();

        if (header.getStatus() != InventoryRecordStatus.DRAFT) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE,
                    "확정된 출고의 LOT는 삭제할 수 없습니다."
            );
        }

        // 헤더 출고 수량(LOT 합계) 차감
        header.decreaseQuantity(lot.getQuantity());

        // 연관관계 정리(컬렉션에서 제거하면 orphanRemoval=true에 의해 삭제됨)
        header.getLotItems().remove(lot);

        // 혹시나 명시적으로 삭제하고 싶다면 아래 한 줄을 유지해도 무방
        // inventoryOutLotRepository.delete(lot);
    }

    /**
     * 출고 헤더(logId) 기준 LOT 상세 목록.
     *
     * 로그 뷰의 logId(예: 1000000001)를 받아서 실제 출고 PK로 언랩 후 조회한다.
     */
    @Transactional(readOnly = true)
    public List<InventoryOutLotDetailRowDTO> getOutDetailByOutId(Long outLogId) {
        long outId = unwrap(outLogId); // 1000000001 → 1

        List<InventoryOutLot> lots = inventoryOutLotRepository.findByOutId(outId);

        return lots.stream()
                .map(lot -> InventoryOutLotDetailRowDTO.builder()
                        .lotNo(lot.getBatch().getLotNo())
                        .outDate(lot.getOut().getOutDate())
                        .quantity(lot.getQuantity())
                        .remainingQuantity(lot.getBatch().getQuantity())
                        .storeName(
                                lot.getOut().getStore() != null
                                        ? lot.getOut().getStore().getName()
                                        : null
                        )
                        .build())
                .toList();
    }
}
