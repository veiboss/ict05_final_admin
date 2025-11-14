package com.boot.ict05_final_admin.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 재고 로그 DTO.
 *
 * <p>본사 재고 로그 화면과 엑셀 다운로드에 공통 사용되는 단일 행 표현 객체.</p>
 * <ul>
 *   <li>입고(INCOME), 출고(OUTGO), 조정(ADJUST)을 모두 표현</li>
 *   <li>로그 후 재고, 단가, 점포 정보, LOT 상세용 배치 ID 포함</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "InventoryLogDTO", description = "재고 로그 항목 DTO")
public class InventoryLogDTO {

    /** 로그 ID (각 유형별 PK: IN=inventory_in_id, OUT=inventory_out_id, ADJUST=inventory_adjustment_id) */
    @Schema(description = "로그 ID", example = "12345")
    private Long logId;

    /** 로그 발생 일시 */
    @Schema(description = "로그 일시", example = "2025-11-10T13:45:12")
    private LocalDateTime logDate;

    /** 로그 유형(INCOME / OUTGO / ADJUST) */
    @Schema(description = "로그 유형(INCOME / OUTGO / ADJUST)", example = "OUTGO")
    private String logType;

    /** 변동 수량(+입고, -출고, ±조정) */
    @Schema(description = "변동 수량(+입고, -출고, ±조정)", example = "10.000")
    private BigDecimal quantity;

    /** 로그 반영 후 재고 수량 */
    @Schema(description = "로그 반영 후 재고", example = "253.237")
    private BigDecimal stockAfter;

    /** 단가(있을 경우만 세팅: 입고 단가 또는 출고 단가) */
    @Schema(description = "단가(입고/출고 단가)", example = "10000")
    private BigDecimal unitPrice;

    /** 메모 */
    @Schema(description = "메모", example = "입고 반품 처리")
    private String memo;

    /** 가맹점 FK (본사 로그는 null) */
    @Schema(description = "가맹점 ID", example = "3")
    private Long storeId;

    /** 가맹점명 */
    @Schema(description = "가맹점명", example = "신촌점")
    private String storeName;

    /**
     * LOT 상세용 배치 PK.
     *
     * <p>입고(INCOME): 해당 입고 배치 ID(inventory_batch_id)<br>
     * 출고(OUTGO): 헤더 기준이므로 null(LOT 상세는 별도 out_lot 조회)<br>
     * 조정(ADJUST): 조정 대상 배치가 필요한 경우에만 사용, 기본 null</p>
     */
    @Schema(description = "LOT 상세용 배치 ID (입고/조정 시 사용 가능)", example = "59")
    private Long batchId;
}
