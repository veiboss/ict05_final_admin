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
    private String  orderDate;             // 라벨(yyyy-MM-dd 또는 yyyy-MM)
    private String  store;
    private String  material;

    private Long    storeInventoryQty;     // 일말/월말 스냅샷

    // ✅ 일별 전용 노출 컬럼
    private Long    purchaseOrderId;
    private String  purchaseOrderDate;
    private Long    purchaseOrderQty;

    // 계산/지표
    private BigDecimal turnoverRate;       // Used / AvgInventory (소수, %는 뷰에서)
    private Long    profit;                // Sales − Cost
    private BigDecimal margin;             // Profit / Sales
    private BigDecimal avgUsage;           // ✅ 이름 정정(일별=당일, 월별=월평균)

    // 내부키
    private Long    storeId;
    private Long    materialId;
}