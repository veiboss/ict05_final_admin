package com.boot.ict05_final_admin.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * FIFO 후보 배치 DTO
 *
 * <p>출고 미리보기(FIFO) 계산을 위해 재고가 남아있는 배치(로트)를 표현한다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *   <li>batchId: 배치 ID</li>
 *   <li>lotNo: 로트 번호</li>
 *   <li>expirationDate: 유통기한(선택)</li>
 *   <li>available: 배치 잔량</li>
 * </ul>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FifoCandidateDTO {
    /** 배치 ID */
    private Long batchId;

    /** 로트 번호 */
    private String lotNo;

    /** 유통기한 */
    @Schema(type = "string", format = "date")
    private LocalDate expirationDate;

    /** 배치 잔량 */
    private BigDecimal available;
}
