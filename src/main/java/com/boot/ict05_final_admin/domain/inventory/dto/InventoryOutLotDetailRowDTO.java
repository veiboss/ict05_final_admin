package com.boot.ict05_final_admin.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 출고 로그 팝업용 LOT 상세 DTO
 *
 * - 하나의 출고 헤더(outId)에 속한 각 LOT 행을 표현
 * - JS에서 기대하는 필드명(lotNo, outDate, quantity, remainingQuantity)에 맞춤
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryOutLotDetailRowDTO {

    /** LOT 번호 */
    @Schema(description = "LOT 번호", example = "MAT-20251114-001")
    private String lotNo;

    /** 출고 일시 */
    @Schema(description = "출고 일시", example = "2025-11-14T23:40:00")
    private LocalDateTime outDate;

    /** 출고 수량 */
    @Schema(description = "출고 수량", example = "50.000")
    private BigDecimal quantity;

    /** 해당 LOT 남은 수량 */
    @Schema(description = "출고 후 LOT 잔량", example = "30.000")
    private BigDecimal remainingQuantity;

    /** 출고 대상 가맹점명 */
    @Schema(description = "가맹점명", example = "신촌점")
    private String storeName;
}
