package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;

/** 시간·요일 분석 표 행 DTO */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeRowDto {
    private String storeName;       // "Total" 또는 점포명
    private String hourSlot;        // "HH:00-HH:59"
    private String dayOfWeek;       // "일"~"토"
    private Long   orderId;         // 주문 ID(집계행은 null)
    private BigDecimal orderAmount; // 주문 금액
    private String category;
    private String menu;
    private String orderType;       // visit/takeout/delivery
    private String orderDate;       // 'yyyy-MM-dd'
}
