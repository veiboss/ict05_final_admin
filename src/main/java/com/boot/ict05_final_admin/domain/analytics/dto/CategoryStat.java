package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryStat {
	private Long categoryId;
	private String categoryName;
	private Long units;              // 판매수량 합계 (SUM(quantity))
	private BigDecimal sales;        // 매출 합계 (SUM(line_total))
}