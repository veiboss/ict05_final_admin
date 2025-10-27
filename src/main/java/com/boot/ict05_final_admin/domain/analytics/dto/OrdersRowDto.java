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

    private String storeName;
    private Long countOrder;
    private BigDecimal salesOrder;

    private String category;
    private String menu;

    private Long countMenu;
    private BigDecimal salesMenu;

    private String orderType;   // visit/takeout/delivery
    private String orderDate;   // 'yyyy-MM-dd' 또는 'yyyy-MM-01'
}
