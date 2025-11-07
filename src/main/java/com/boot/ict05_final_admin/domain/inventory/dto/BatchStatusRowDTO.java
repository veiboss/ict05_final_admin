package com.boot.ict05_final_admin.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * HQ 로트 현황 DTO
 *
 * <p>본사 재고의 로트(배치) 단위 상태를 표시한다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *   <li>batchId: 배치 고유 ID</li>
 *   <li>lotNo: 로트 번호(예: LOT251107-003)</li>
 *   <li>receivedDate: 입고일시</li>
 *   <li>expirationDate: 유통기한(일 단위)</li>
 *   <li>receivedQty: 입고 수량</li>
 *   <li>remainQty: 현재 잔량</li>
 *   <li>unitPrice: 입고 단가(매입가)</li>
 * </ul>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BatchStatusRowDTO {

    /** 배치 고유 ID */
    private Long batchId;

    /** 로트 번호 */
    private String lotNo;

    /** 입고일시 */
    @Schema(type = "string", format = "date-time")
    private LocalDateTime receivedDate;

    /** 유통기한(일 단위) */
    @Schema(type = "string", format = "date")
    private LocalDate expirationDate;

    /** 입고 수량 */
    private BigDecimal receivedQty;

    /** 현재 잔량 */
    private BigDecimal remainQty;

    /** 입고 단가(매입가) */
    private BigDecimal unitPrice;
}
