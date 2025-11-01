package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class OrdersCardsDto {

    private Long transaction;                 // YTD 주문건수
    private BigDecimal deliverySales;         // YTD 배달 매출
    private BigDecimal takeoutSales;          // YTD 포장 매출
    private BigDecimal visitSales;            // YTD 방문 매출

    private java.util.List<CategoryStat> categoriesByCount;  // 수량 기준 내림차순 정렬
    private java.util.List<CategoryStat> categoriesBySales;  // 매출 기준 내림차순 정렬

    private Long menuCount;                   // YTD '판매수량 합계' (SUM(quantity))
    private java.util.List<TopMenuItem> topMenus;

}
