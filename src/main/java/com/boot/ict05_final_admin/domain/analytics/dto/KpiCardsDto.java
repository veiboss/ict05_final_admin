package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KpiCardsDto {
    private BigDecimal sales;    // YTD 총매출
    private Long transaction;    // YTD 결제건수
    private BigDecimal upt;      // 총판매메뉴수 / 결제건수
    private BigDecimal ads;      // 총매출 / 결제건수
    private BigDecimal aur;      // 총매출 / 총판매메뉴수

    private BigDecimal compMoM;  // (MTD / PMTD) - 1
    private BigDecimal compYoY;  // (YTD / YTD-1Y) - 1

    // 주문형태 매출 비중(%) - 0~100
    private BigDecimal visitRatio;
    private BigDecimal takeoutRatio;
    private BigDecimal deliveryRatio;
}