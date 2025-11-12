package com.boot.ict05_final_admin.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 재고 로그 DTO.
 *
 * <p>목적: 본사 재고 로그 화면 및 엑셀 다운로드에 공통 사용.</p>
 * <ul>
 *   <li>식별자, 일시, 유형, 수량, 로그 후 재고, 단가, 메모, 점포명 포함</li>
 * </ul>
 *
 * @author 김주연
 * @since 2025-11-12
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "InventoryLogDTO", description = "재고 로그 항목 DTO")
public class InventoryLogDTO {

    /** 로그 ID (PK) */
    @Schema(description = "로그 ID", example = "12345")
    private Long logId;

    /** 로그 발생 일시 */
    @Schema(description = "로그 일시", example = "2025-11-10T13:45:12")
    private LocalDateTime logDate;

    /** 로그 유형(INCOME, OUTCOME, ADJUST 등) */
    @Schema(description = "로그 유형", example = "INCOME")
    private String logType;

    /** 변동 수량(+입고, -출고, ±조정) */
    @Schema(description = "변동 수량", example = "10.000")
    private BigDecimal quantity;

    /** 로그 반영 후 재고 수량 */
    @Schema(description = "로그 반영 후 재고", example = "253.237")
    private BigDecimal stockAfter;

    /** 단가(있을 경우) */
    @Schema(description = "단가", example = "10000")
    private BigDecimal unitPrice;

    /** 메모 */
    @Schema(description = "메모", example = "입고 반품 처리")
    private String memo;

    /** 가맹점 FK */
    @Schema(description = "가맹점 ID")
    private Long storeId;

    /** 가맹점명 */
    @Schema(description = "가맹점명")
    private String storeName;
}
