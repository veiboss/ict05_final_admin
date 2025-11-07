package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOut;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryOutLot;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutLotRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryOutRepository;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.store.entity.Store;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 출고 서비스
 *
 * <p>수주(배송상태 전환) 시점에 본사 재고를 FIFO로 출고 확정한다.</p>
 * <ul>
 *   <li>confirmOut(...): FIFO 자동 분배 → 헤더/로트 저장</li>
 *   <li>confirmOutWithAllocation(...): 외부 분배안으로 확정</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class InventoryOutService {

    private final InventoryOutRepository outRepo;
    private final InventoryOutLotRepository outLotRepo;
    private final InventoryBatchRepository batchRepo;

    @PersistenceContext
    private EntityManager em;

    /**
     * 출고 확정(FIFO 자동)
     *
     * @param materialId 재료ID
     * @param storeId 가맹점ID(null 허용. 내부 사용/폐기 등)
     * @param totalQty 총 출고수량
     * @param outDate 출고일시(null이면 now)
     * @param memo 메모
     * @return 저장된 출고ID
     */
    @Transactional
    public Long confirmOut(Long materialId,
                           Long storeId,
                           BigDecimal totalQty,
                           LocalDateTime outDate,
                           String memo) {

        List<AllocItem> alloc = allocateFifo(materialId, totalQty); // HQ 배치만 사용

        InventoryOut out = InventoryOut.builder()
                .material(em.getReference(Material.class, materialId))
                .store(storeId != null ? em.getReference(Store.class, storeId) : null)
                // 엔티티 필드명이 outDate라면 .outDate(...) 로 변경
                .outDate(outDate != null ? outDate : LocalDateTime.now())
                .quantity(totalQty)
                .memo(memo)
                .build();
        out = outRepo.save(out);

        persistLotsAndDecrement(out, alloc);
        return out.getId();
    }

    /**
     * 출고 확정(외부 분배안)
     *
     * @param materialId 재료ID
     * @param storeId 가맹점ID
     * @param totalQty 총 수량
     * @param outDate 출고일시
     * @param memo 메모
     * @param allocation key=batchId, value=qty
     * @return 출고ID
     */
    @Transactional
    public Long confirmOutWithAllocation(Long materialId,
                                         Long storeId,
                                         BigDecimal totalQty,
                                         LocalDateTime outDate,
                                         String memo,
                                         Map<Long, BigDecimal> allocation) {

        BigDecimal sum = allocation.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(totalQty) != 0) {
            throw new IllegalArgumentException("합계 불일치: total=" + totalQty + ", allocSum=" + sum);
        }

        // 유효성: 각 배치 잔량 충분한지 사전 체크
        for (Map.Entry<Long, BigDecimal> e : allocation.entrySet()) {
            Long batchId = e.getKey();
            BigDecimal need = e.getValue();
            InventoryBatch b = batchRepo.findById(batchId)
                    .orElseThrow(() -> new IllegalArgumentException("배치 없음: " + batchId));
            if (b.getQuantity().compareTo(need) < 0) {
                throw new IllegalStateException("잔량 부족: batch=" + batchId + ", need=" + need + ", remain=" + b.getQuantity());
            }
        }

        InventoryOut out = InventoryOut.builder()
                .material(em.getReference(Material.class, materialId))
                .store(storeId != null ? em.getReference(Store.class, storeId) : null)
                .outDate(outDate != null ? outDate : LocalDateTime.now()) // 필드명이 outDate면 교체
                .quantity(totalQty)
                .memo(memo)
                .build();
        out = outRepo.save(out);

        List<AllocItem> alloc = allocation.entrySet().stream()
                .map(e -> new AllocItem(e.getKey(), e.getValue()))
                .toList();

        persistLotsAndDecrement(out, alloc);
        return out.getId();
    }

    // ---------- 내부 유틸 ----------

    /** FIFO 분배. HQ 배치만, 잔량>0, 유통기한→입고일 순. 부족 시 예외. */
    private List<AllocItem> allocateFifo(Long materialId, BigDecimal requestedQty) {
        BigDecimal remain = requestedQty;
        List<AllocItem> list = new ArrayList<>();

        for (InventoryBatch b : batchRepo.findFifoCandidates(materialId)) {
            if (remain.signum() <= 0) break;
            BigDecimal take = b.getQuantity().min(remain);
            if (take.signum() > 0) {
                list.add(new AllocItem(b.getId(), take));
                remain = remain.subtract(take);
            }
        }
        if (remain.signum() > 0) {
            throw new IllegalStateException("HQ 재고 부족: 부족수량=" + remain);
        }
        return list;
    }

    /** 아이템 저장 + 원자 차감 */
    private void persistLotsAndDecrement(InventoryOut out, List<AllocItem> alloc) {
        for (AllocItem it : alloc) {
            int ok = batchRepo.decrementQuantity(it.batchId(), it.qty());
            if (ok == 0) {
                throw new IllegalStateException("동시성 충돌 또는 잔량 부족: batchId=" + it.batchId());
            }
            InventoryOutLot lot = InventoryOutLot.builder()
                    .out(out)
                    .batch(em.getReference(InventoryBatch.class, it.batchId()))
                    .quantity(it.qty())
                    .build();
            outLotRepo.save(lot);
        }
    }

    /** 분배 아이템 레코드 */
    private record AllocItem(Long batchId, BigDecimal qty) {}
}
