package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.AdjustmentReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 재고 조정 DTO
 *
 * - adjustInventory 요청/응답 공용
 * - 상세 조회(getAdjustDetail) 응답에도 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAdjustDTO {

    // 팝업용 메타 필드
    private Long logId;                 // adjustment_id
    private String type;                // "ADJUST"
    private LocalDateTime logDate;      // createdAt

    /** 본사 재고 ID */
    @NotNull
    @Comment("본사 재고 ID")
    private Long inventoryId;

    /** 재료 ID */
    @NotNull
    @Comment("재료 ID")
    private Long material;

    /** 조정 후 수량 */
    @NotNull(message = "조정 후 수량을 입력해주세요")
    @PositiveOrZero
    @Comment("조정 후 수량")
    private BigDecimal quantityAfter;

    /** 비고 / 조정 사유 (자유 입력) */
    @Comment("비고 / 조정 사유")
    private String memo;

    /** 조정 사유 분류 (MANUAL, DAMAGE, LOSS 등) */
    @Comment("조정 사유 분류")
    private AdjustmentReason reason;


    /** 조정 전 수량 */
    private BigDecimal quantityBefore;

    /** 조정 수량 */
    private BigDecimal difference;
}
