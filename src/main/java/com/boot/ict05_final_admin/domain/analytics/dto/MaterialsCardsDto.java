package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class MaterialsCardsDto {
    private LocalDate ytdStart;
    private LocalDate ytdEnd;

    private BigDecimal hqStockQtyNow;     // 현재 본사 재고 수량
    private BigDecimal storeStockQtyNow;  // 현재 가맹점 총 재고 수량(선택 매장 합)
    private BigDecimal orderQtyYtd;       // YTD 자재 발주 수량
    private BigDecimal consumptionQtyYtd; // YTD 자재 소진 수량(레시피×판매수량)
    private BigDecimal salesAmountYtd;    // YTD 매출액
    private BigDecimal cogsYtd;           // YTD 소진원가
    private BigDecimal profit;            // 매출-원가
    private BigDecimal marginRate;        // (이익/매출)*100
    private BigDecimal turnover;          // 회전율: COGS / 평균재고(근사)
}