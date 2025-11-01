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
    private String  orderDate;             // YYYY-MM-DD or YYYY-MM (라벨)
    private String  store;                 // 가맹점명
    private String  material;              // 재료명

    private Long    storeInventoryQty;     // 현재 점포 재고수량 (on-hand)

    // ✅ 일별(DAY) 전용 노출 컬럼
    private Long    purchaseOrderId;       // 발주(입고) ID (ro.id 추정: 필드명 다르면 맞춰주세요)
    private String  purchaseOrderDate;     // 발주(입고) 일자(YYYY-MM-DD)
    private Long    purchaseOrderQty;      // 발주 수량(= rod.detailCount 합)

    // (혼동 방지를 위해 테이블에선 미사용)
    private BigDecimal orderAmount;        // 금액(rod.detailCount*unit_price) — 필요 시만 사용

    // 계산값(Phase A는 0)
    private BigDecimal turnoverRate;       // Used / AvgInventory
    private BigDecimal profit;             // Sales − Cost
    private BigDecimal margin;             // Profit / Sales * 100
    private BigDecimal avgDailyUsage;      // (임시) 발주수량/일수

    // 내부키
    private Long    storeId;
    private Long    materialId;
}