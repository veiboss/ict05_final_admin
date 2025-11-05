package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChartSeriesDto {
    private String name;                 // "Total - VISIT", "강남점 - DELIVERY" 등
    private List<BigDecimal> data;       // 라벨 순서에 맞는 값 배열
}
