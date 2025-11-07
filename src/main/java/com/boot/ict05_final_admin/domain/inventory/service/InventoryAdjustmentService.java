package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.entity.AdjustmentReason;
import com.boot.ict05_final_admin.domain.inventory.entity.HqInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryAdjustment;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryAdjustmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 재고 조정 서비스 (InventoryAdjustmentService)
 *
 * <p>대상은 <b>재고ID</b> 기준이다. Δ(증감치)는 {@code difference}를 사용한다.</p>
 */
@Service
@RequiredArgsConstructor
public class InventoryAdjustmentService {

    private final InventoryAdjustmentRepository repo;

    @PersistenceContext
    private EntityManager em;

    /**
     * 기간별 조정 이력 조회
     *
     * @param inventoryId 재고 ID
     * @param from 시작일시(포함)
     * @param to 종료일시(포함)
     * @return 조정 엔티티 목록
     */
    public List<InventoryAdjustment> findByInventoryAndPeriod(Long inventoryId,
                                                              LocalDateTime from,
                                                              LocalDateTime to) {
        return repo.findByInventoryAndPeriod(inventoryId, from, to);
    }

    /**
     * 기간 내 Δ(증감치) 합계
     *
     * <p>조정 후 재고수준이 아니라, {@code difference}의 합을 반환한다.</p>
     *
     * @param inventoryId 재고 ID
     * @param from 시작일시
     * @param to 종료일시
     * @return Δ 합계. 없으면 0
     */
    public BigDecimal sumAdjustmentDelta(Long inventoryId,
                                         LocalDateTime from,
                                         LocalDateTime to) {
        return repo.sumQuantityByInventoryAndPeriod(inventoryId, from, to);
    }

    /**
     * 마지막 조정 시각 조회
     *
     * @param inventoryId 재고 ID
     * @return 가장 최근 조정 일시. 없으면 null
     */
    public LocalDateTime lastAdjustmentAt(Long inventoryId) {
        return repo.lastAdjustmentAtByInventory(inventoryId);
    }

    /**
     * 조정 생성
     *
     * <p>입력된 {@link AdjustCreateRequest}의 값을 그대로 기록한다.
     * 호출자가 {@code quantityBefore}/{@code quantityAfter}를 계산해 전달해야 한다.</p>
     *
     * @param req 생성 요청
     * @return 생성된 조정 ID
     */
    @Transactional
    public Long createAdjustment(AdjustCreateRequest req) {
        InventoryAdjustment a = InventoryAdjustment.builder()
                // link 메서드 없이 JPA reference로 FK 연결
                .inventory(em.getReference(HqInventory.class, req.getInventoryId()))
                .difference(req.getDifference())
                .reason(req.getReason())
                .memo(req.getMemo())
                .createdAt(req.getAt() != null ? req.getAt() : LocalDateTime.now())
                .quantityBefore(req.getQuantityBefore())
                .quantityAfter(req.getQuantityAfter())
                .build();
        return repo.save(a).getId();
    }

    /** 조정 생성 요청 DTO */
    @Data
    @Builder
    public static class AdjustCreateRequest {
        private Long inventoryId;
        private BigDecimal difference;
        private AdjustmentReason reason;
        private String memo;
        private LocalDateTime at;
        private BigDecimal quantityBefore;
        private BigDecimal quantityAfter;
    }
}
