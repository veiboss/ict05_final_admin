package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;

/** 시간·요일 분석 표 행 DTO (OrdersRowDto와 일관성 유지: orderType/orderDate → String) */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeRowDto {
    private String     date;        // ✅ 추가: "yyyy-MM-dd" 또는 "yyyy-MM" 라벨

    private String     storeName;
    private String     hourSlot;    // "HH:00-HH:59"
    private String     dayOfWeek;   // "일"~"토"
    private Long       orderId;
    private BigDecimal orderAmount;
    private String     category;
    private String     menu;
    private String     orderType;   // "VISIT" | "TAKEOUT" | "DELIVERY"
    private String     orderDate;   // "yyyy-MM-dd HH:mm"
}
