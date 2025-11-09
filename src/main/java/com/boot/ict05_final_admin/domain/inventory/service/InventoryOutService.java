package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.OutConfirmRequest;
import com.boot.ict05_final_admin.domain.inventory.dto.OutPreviewItemDTO;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchQueryRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutLotRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutRepository;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOut;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOutLot;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.store.entity.Store;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 출고 서비스
 *
 * <p>FIFO 미리보기 슬라이싱과 출고 확정만 담당한다. 조회는 QueryRepository로 위임한다.</p>
 */
@Service
@RequiredArgsConstructor
public class InventoryOutService {

    private final InventoryBatchQueryRepository batchQueryRepo;
    private final InventoryOutRepository outRepo;
    private final InventoryOutLotRepository outLotRepo;
    private final EntityManager em;

    /**
     * FIFO 미리보기.
     *
     * @param materialId 재료 ID
     * @param qty   총 출고 수량
     * @return 배치 분할 미리보기
     */
    public List<OutPreviewItemDTO> previewFifo(Long materialId, BigDecimal qty) {
        var candidates = batchQueryRepo.findAvailableBatchesForFifo(materialId);
        var remain = qty;
        List<OutPreviewItemDTO> plan = new java.util.ArrayList<>();
        for (var c : candidates) {
            if (remain.signum() <= 0) break;
            var take = c.getAvailable().min(remain);
            if (take.signum() > 0) {
                plan.add(OutPreviewItemDTO.builder()
                        .batchId(c.getBatchId())
                        .lotNo(c.getLotNo())
                        .qty(take)
                        .expirationDate(c.getExpirationDate())
                        .build());
                remain = remain.subtract(take);
            }
        }
        return plan;
    }

    /**
     * 출고 확정.
     *
     * @param materialId 재료 ID
     * @param storeId    가맹점 ID(선택)
     * @param totalQty   총 출고 수량
     * @param outDate    출고일시
     * @param memo       비고
     * @return 출고 헤더 ID
     */
    @Transactional
    public Long confirmOut(Long materialId, Long storeId, BigDecimal totalQty, LocalDateTime outDate, String memo) {
        InventoryOut out = InventoryOut.builder()
                .material(em.getReference(Material.class, materialId))
                .store(storeId != null ? em.getReference(Store.class, storeId) : null)
                .outDate(outDate != null ? outDate : LocalDateTime.now())
                .quantity(totalQty)
                .memo(memo)
                .build();
        out = outRepo.save(out);

        // FIFO 배치 차감 + out_lot 생성
        var plan = previewFifo(materialId, totalQty);
        for (var p : plan) {
            outLotRepo.save(InventoryOutLot.builder()
                    .out(out)
                    .batch(em.getReference(com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch.class, p.getBatchId()))
                    .quantity(p.getQty())
                    .build());
            // 배치 잔량 차감은 트리거 또는 별도 서비스에서 처리 중이라면 생략.
            // 배치를 직접 차감하려면 InventoryBatchCommandRepository 메서드 호출로 일괄 업데이트.
        }
        return out.getId();
    }

    // 확정(컨트롤러 DTO용 오버로드)
    @Transactional
    public Long confirmOut(OutConfirmRequest req) {
        return confirmOut(req.getMaterialId(),
                req.getStoreId(),
                req.getTotalQty(),
                req.getOutDate(),
                req.getMemo());
    }

    /** 출고 헤더 삭제. */
    @Transactional
    public void deleteOut(Long outId) {
        outRepo.deleteById(outId);
    }
}
