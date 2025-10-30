package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 재료 분석 테이블 행 DTO.
 *
 * <p>레시피/발주 연동 전 최소 버전. (추후 수식 확장 가능)</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialsRowDto {
    private String  orderDate;            // YYYY-MM-DD (집계 라벨)
    private String  store;                // Store (이름)
    private String  material;             // Material (이름)

    private Long    storeInventoryQty;    // 현재 점포 재고수량
    private BigDecimal orderAmount;       // 발주 금액(원) - Σ(rod.quantity * rod.unit_price)
    private BigDecimal turnoverRate;      // Used / AvgInventory
    private BigDecimal profit;            // Sales − Cost
    private BigDecimal margin;            // Profit / Sales * 100 (%)
    private BigDecimal avgDailyUsage;     // Used / dayCount  (수량/일)

    // 내부 식별용(프론트에 노출X) - 필요시
    private Long    storeId;
    private Long    materialId;
}
