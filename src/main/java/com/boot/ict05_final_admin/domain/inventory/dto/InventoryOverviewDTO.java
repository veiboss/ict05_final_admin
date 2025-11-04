package com.boot.ict05_final_admin.domain.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * 재료별 재고 요약 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryOverviewDTO {

    /** 최근 기간(기본 30일)의 일평균 출고량 */
    private BigDecimal avgDailyUse;

    /** 재고 커버리지(DOH, Days of Holding) = 현재수량 ÷ 일평균사용량 */
    private BigDecimal doh;

    /** 마지막 변동일 (입고, 출고, 조정 중 가장 최근) */
    private LocalDate lastMoveAt;

    /** 현재 재고가치 = 현재수량 × 이동평균단가 */
    private BigDecimal stockValue;
}
