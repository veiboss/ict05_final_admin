package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * 재료분석 Total 계산을 위한 원천 합계 값들 (Repo → Service 파생지표 계산용)
 * - 서비스에서 TurnoverRate, MarginRate, AvgUsage 등을 계산한다.
 */
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class MaterialsTotalsRaw {
	/** 현재 기준 가맹점 재고 총량 (store_inventory.inventory_quantity) */
	private BigDecimal currentStoreInventoryQty;

	/** YTD 발주수량 합 (Σ rod.detailCount) */
	private Long orderVolumeQty;

	/** YTD 재료 소진량 합 (Σ cod.quantity × mr.recipeQty) */
	private BigDecimal usedQty;

	/** YTD 재료 ‘원가’ 합 (Σ rod.detailUnitPrice × rod.detailCount) */
	private BigDecimal costSum;

	/** YTD 재료 ‘판매가 기준액’ 합 (Σ sm.sellingPrice × rod.detailCount) */
	private BigDecimal sellingSum;
}