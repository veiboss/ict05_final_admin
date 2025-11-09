package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.AdjustmentReason;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 재고 수량 조정 생성 요청 DTO
 *
 * <p>재고 수량 조정 등록 시 사용되는 데이터 전송 객체(DTO)이다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *   <li>inventoryId: 조정 대상 재고 ID</li>
 *   <li>difference: 증감 수량(양수=증가, 음수=감소)</li>
 *   <li>reason: 조정 사유(ENUM)</li>
 *   <li>memo: 비고</li>
 *   <li>at: 조정 시각</li>
 *   <li>quantityBefore: 조정 전 수량(선택, 검증·로그용)</li>
 *   <li>quantityAfter: 조정 후 수량(선택, 검증·로그용)</li>
 * </ul>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AdjustCreateRequestDTO {

    /** 조정 대상 재고 ID */
    private Long inventoryId;

    /** 증감 수량(양수=증가, 음수=감소) */
    private BigDecimal difference;

    /** 조정 사유 */
    private AdjustmentReason reason;

    /** 비고 */
    private String memo;

    /** 조정 시각 */
    @Schema(type = "string", format = "date-time")
    private LocalDateTime at;

    /** 조정 전 수량(선택) */
    private BigDecimal quantityBefore;

    /** 조정 후 수량(선택) */
    private BigDecimal quantityAfter;
}
