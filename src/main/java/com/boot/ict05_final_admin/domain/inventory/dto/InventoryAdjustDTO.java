package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.InventoryAdjustment.AdjustmentReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

/**
 * 본사 재고 수량 조정 등록 DTO
 *
 * <p>입출고 외의 사유로 재고를 직접 수정할 때 사용하는 입력용 DTO.</p>
 */
@Data
public class InventoryAdjustDTO {

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
}
