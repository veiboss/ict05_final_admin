package com.boot.ict05_final_admin.domain.inventory.dto;

import lombok.Data;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 본사 재고 LOT 상세 DTO
 *
 * <p>inventory_batch 1건에 대한 상세 정보.</p>
 */
@Data
public class InventoryLotDetailDTO {

    /** 배치 ID */
    @Comment("배치 ID")
    private Long batchId;

    /** 재료 ID */
    @Comment("재료 ID")
    private Long materialId;

    /** 재료 코드 */
    @Comment("재료 코드")
    private String materialCode;

    /** 재료명 */
    @Comment("재료명")
    private String materialName;

    /** LOT 번호 */
    @Comment("LOT 번호")
    private String lotNo;

    /** 입고일시 */
    @Comment("입고일시")
    private LocalDateTime receivedDate;

    /** 유통기한(유효일자) */
    @Comment("유통기한")
    private LocalDate expirationDate;

    /** 입고 수량 */
    @Comment("입고 수량")
    private BigDecimal receivedQuantity;

    /** 현재 잔량 */
    @Comment("현재 잔량")
    private BigDecimal remainingQuantity;

    /** 단가 */
    @Comment("단가")
    private BigDecimal unitPrice;




    // 팝업 상단용 (필요하면)
    private Long logId;              // v_inventory_log 에서 넘겨주면 세팅
    private String type;             // "INCOME"/"OUTGO"
    private LocalDateTime logDate;   // 해당 로그 시각
}
