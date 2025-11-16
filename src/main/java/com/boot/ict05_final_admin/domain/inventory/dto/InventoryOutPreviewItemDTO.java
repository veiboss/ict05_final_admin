package com.boot.ict05_final_admin.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 출고 FIFO 분배 미리보기/확정용 DTO
 *
 * <p>요청 수량을 각 배치로 분배한 결과를 표현한다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *   <li>batchId: 배치 고유 ID</li>
 *   <li>lotNo: 로트 번호</li>
 *   <li>expirationDate: 유통기한(일 단위)</li>
 *   <li>qty: 해당 배치에서 출고할 수량</li>
 * </ul>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class InventoryOutPreviewItemDTO {

    /** 배치 고유 ID */
    private Long batchId;

    /** 로트 번호 */
    private String lotNo;

    /** 유통기한(일 단위) */
    @Schema(type = "string", format = "date")
    private LocalDate expirationDate;

    /** 출고 수량 */
    private BigDecimal qty;
}