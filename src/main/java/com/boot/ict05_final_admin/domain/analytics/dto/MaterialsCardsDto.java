package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class MaterialsCardsDto {
    /** 본사(물류창고) 현재 총 재고수량 */
    private Long currentOfficeInventoryQty;

    /** 전체 가맹점 현재 총 재고수량 (가맹점 필터가 있으면 그 범위로) */
    private Long currentTotalStoreInventoryQty;

    /** 조회기간 발주 수량 합( Order Volume = Σ(발주수량) ) */
    private Long orderVolumeQty;

    /** 조회기간 소진 수량 합( Used Quantity ) */
    private Long usedQty;

    /** 회전율( Turnover Rate ) = Used / AvgInventory */
    private BigDecimal turnoverRate;

    /** 매출액( Sales Amount ) — BOM 준비 전까지 ‘관련 메뉴 매출 귀속액’은 0 처리/옵션 */
    private BigDecimal salesAmount;

    /** 이익( Profit Amount ) = Sales − Cost  */
    private BigDecimal profitAmount;

    /** 평균 마진율( Avg.Margin, % ) = Profit / Sales * 100  */
    private BigDecimal avgMargin;
}