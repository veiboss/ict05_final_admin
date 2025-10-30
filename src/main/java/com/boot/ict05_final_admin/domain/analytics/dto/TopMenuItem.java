package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopMenuItem {
    private Long menuId;
    private String menuName;
    private Long quantity;                // 누적 판매수량
    private BigDecimal sales;             // 누적 매출
    private BigDecimal ratio;             // 전체 라인매출 대비 비중(%)
}
