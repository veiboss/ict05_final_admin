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

    private String storeName;      // "Total" 또는 점포명
    private String materialName;   // 재료명

    private Long storeStock;       // 매장 보유 재고
    private Long orderQty;         // 선택기간 발주량(옵션)

    private BigDecimal turnover;   // 회전율(옵션)
    private BigDecimal salesAmount;// 매출액(옵션)
    private BigDecimal profit;     // 이익금(옵션)
    private BigDecimal marginRate; // 마진율(옵션)
}
