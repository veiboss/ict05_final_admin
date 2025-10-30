package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class OrdersCardsDto {

    private Long transaction;    // YTD 주문건수
    private BigDecimal deliverySales;    // YTD 배달 매출
    private BigDecimal takeoutSales;    // YTD 포장 매출
    private BigDecimal visitSales;    // YTD 방문 매출
    private Long categoryCount;  // YTD 판매된 카테고리 수(DISTINCT)
    private String categoryName;           // YTD 최상위 카테고리명
    private BigDecimal categoryRatio;    // YTD 카테고리별 비중
    private Long menuCount;      // YTD 판매된 메뉴 수(DISTINCT)
    private BigDecimal menuTop3Ratio;    // YTD Top3 메뉴 비중
    private List<TopMenuItem> topMenus;    // YTD Top3 메뉴 이름
}