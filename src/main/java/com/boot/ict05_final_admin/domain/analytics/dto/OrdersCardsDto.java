package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class OrdersCardsDto {
    private BigDecimal sales;    // YTD 총매출
    private Long transaction;    // YTD 주문건수
    private Long categoryCount;  // YTD 판매된 카테고리 수(DISTINCT)
    private Long menuCount;      // YTD 판매된 메뉴 수(DISTINCT)

    private BigDecimal visitRatio;    // 주문형태 비중(%)
    private BigDecimal takeoutRatio;
    private BigDecimal deliveryRatio;
}