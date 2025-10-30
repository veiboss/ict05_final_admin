package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 주문 분석 테이블 행 DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersRowDto {

    /** 기간 라벨(일별: yyyy-MM-dd, 월별: yyyy-MM) */
    private String date;
    private String storeName;
    private String category;
    private String menu;
    private BigDecimal menuSales;
    private Long menuCount;
    private Long orderCount;
    private BigDecimal orderSales;
    private String orderType;   // visit/takeout/delivery
    private String orderDate;   // 'yyyy-MM-dd' 또는 'yyyy-MM-01'
}
