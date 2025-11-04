package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.InventoryOverviewDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.HqInventory;

import com.boot.ict05_final_admin.domain.inventory.entity.Material;

import com.boot.ict05_final_admin.domain.inventory.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * 본사 재고 요약 서비스
 * <p>
 * - 특정 재료(materialId)에 대한 상단 요약 정보 계산
 * - Material 엔티티를 함께 참조하여 단위(unit) 등 표시용 데이터 제공
 * - 재고 현황(KPI) 계산: 일평균사용량, 커버리지, 마지막변동일, 재고가치
 */
@Service
@RequiredArgsConstructor
public class InventoryOverviewService {

    private final InventoryRepository hqRepo;
    private final InventoryOutRepository outRepo;
    private final InventoryInRepository inRepo;
    private final InventoryLogViewRepository logRepo;
    private final MaterialRepository materialRepo;

    /**
     * 재료별 재고 요약 조회
     *
     * @param materialId 재료 ID (Material.id)
     * @param from       조회 시작일 (null이면 최근 30일)
     * @param to         조회 종료일 (null이면 오늘)
     * @return InventoryOverviewDTO
     */
    @Transactional
    public InventoryOverviewDTO getOverview(Long materialId, LocalDate from, LocalDate to) {

        LocalDate today = LocalDate.now();
        LocalDate f = (from != null) ? from : today.minusDays(29);
        LocalDate t = (to != null) ? to : today;
        LocalDateTime fromDt = f.atStartOfDay();
        LocalDateTime toDt = t.plusDays(1).atStartOfDay().minusNanos(1);
        int days = (int) (java.time.Duration.between(fromDt, toDt).toDays() + 1);

        // === 1. 재료와 재고 조회 ===
        Material material = materialRepo.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("해당 재료를 찾을 수 없습니다."));
        HqInventory inv = hqRepo.findByMaterialId(materialId)
                .orElseThrow(() -> new IllegalArgumentException("해당 재료의 본사 재고가 없습니다."));

        BigDecimal currentQty = inv.getQuantity();

        // === 2. 사용량 및 단가 계산 ===
        BigDecimal outSum = outRepo.sumOutQty(materialId, fromDt, toDt);
        BigDecimal avgDailyUse = days > 0
                ? outSum.divide(BigDecimal.valueOf(days), 3, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal amt = inRepo.sumInAmount(materialId);
        BigDecimal qty = inRepo.sumInQty(materialId);
        BigDecimal movingAvg = qty.compareTo(BigDecimal.ZERO) > 0
                ? amt.divide(qty, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // === 3. 재고가치 (단위 환산 포함) ===
        BigDecimal conversionRate = BigDecimal.valueOf(material.getConversionRate()); // 1000
        BigDecimal stockValue = currentQty
                .divide(conversionRate, 3, RoundingMode.HALF_UP)
                .multiply(movingAvg)
                .setScale(0, RoundingMode.HALF_UP);

        // === 4. 커버리지(DOH) ===
        BigDecimal doh = avgDailyUse.compareTo(BigDecimal.ZERO) > 0
                ? currentQty.divide(avgDailyUse, 1, RoundingMode.HALF_UP)
                : null;

        // === 5. 마지막 변동일 ===
        LocalDateTime lastMove = logRepo.lastMoveAt(materialId);

        // === 6. DTO 생성 ===
        return InventoryOverviewDTO.builder()
                .avgDailyUse(avgDailyUse)
                .doh(doh)
                .lastMoveAt(lastMove != null ? lastMove.toLocalDate() : null)
                .stockValue(stockValue)
                .build();
    }
}
