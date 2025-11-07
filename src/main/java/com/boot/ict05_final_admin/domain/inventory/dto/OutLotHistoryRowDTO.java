package com.boot.ict05_final_admin.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 배치별 출고 이력 행 DTO
 *
 * <p>특정 로트에서 발생한 출고 건의 헤더 정보를 요약한다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *   <li>outId: 출고 헤더 ID</li>
 *   <li>storeId: 출고 대상 가맹점 ID</li>
 *   <li>storeName: 가맹점명</li>
 *   <li>outDate: 출고일시</li>
 *   <li>qty: 출고 수량</li>
 *   <li>memo: 비고</li>
 * </ul>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OutLotHistoryRowDTO {

    /** 출고 헤더 ID */
    private Long outId;

    /** 가맹점 ID */
    private Long storeId;

    /** 가맹점명 */
    private String storeName;

    /** 출고일시 */
    @Schema(type = "string", format = "date-time")
    private LocalDateTime outDate;

    /** 출고 수량 */
    private BigDecimal qty;

    /** 비고 */
    private String memo;
}
