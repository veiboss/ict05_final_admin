package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;

/** 재료분석 Total(요약행) - 서비스에서 파생지표까지 계산 후 전달 */
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class MaterialsTotalDto {
	private BigDecimal totalInventoryQty;   // 현재 가맹점 재고 총량(스냅샷)
	private Long       totalPurchaseOrderQty; // YTD 발주수량
	private BigDecimal turnoverRate;        // Used / AvgInventory (간이식: Used / Current)
	private BigDecimal totalProfit;         // SellingSum - CostSum (발주 관점의 이익 가정)
	private BigDecimal marginRate;          // totalProfit / SellingSum * 100
	private BigDecimal avgUsage;            // Used / 일수
}