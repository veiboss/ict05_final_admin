package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.AdjustCreateRequestDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.*;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryAdjustmentRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    private final InventoryRepository repo;
    private final InventoryAdjustmentRepository adRepo;


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
        return adRepo.findByInventoryAndPeriod(inventoryId, from, to);
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
        return adRepo.sumQuantityByInventoryAndPeriod(inventoryId, from, to);
    }

    /**
     * 마지막 조정 시각 조회
     *
     * @param inventoryId 재고 ID
     * @return 가장 최근 조정 일시. 없으면 null
     */
    public LocalDateTime lastAdjustmentAt(Long inventoryId) {
        return adRepo.lastAdjustmentAtByInventory(inventoryId);
    }


    /**
     * 재고 수량 조정을 등록한다.
     *
     * @param dto 조정 생성 요청 DTO
     * @return 생성된 조정 ID
     */
    @Transactional
    public Long createAdjustment(AdjustCreateRequestDTO dto) {
        var inv = repo.findById(dto.getInventoryId())
                .orElseThrow(() -> new IllegalArgumentException("대상 재고를 찾을 수 없습니다. id=" + dto.getInventoryId()));

        var diff = dto.getDifference();
        if (diff == null) throw new IllegalArgumentException("difference는 필수입니다.");

        var before = dto.getQuantityBefore() != null ? dto.getQuantityBefore() : inv.getQuantity();
        var after  = dto.getQuantityAfter()  != null ? dto.getQuantityAfter()  : before.add(diff);

        var adj = InventoryAdjustment.builder()
                .inventory(inv)
                .createdAt(dto.getAt() != null ? dto.getAt() : LocalDateTime.now())
                .difference(diff)
                .reason(dto.getReason())
                .memo(dto.getMemo())
                .build();

        adRepo.save(adj);
        // 정책상 즉시 반영할 거면: inv.setQuantity(after);

        return adj.getId();
    }
}
