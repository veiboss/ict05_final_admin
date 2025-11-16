package com.boot.ict05_final_admin.domain.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 출고 LOT 상세 DTO (InventoryOutDetailDTO)
 *
 * <p>본사 재고 로그 화면의 LOT 상세 팝업 응답 모델이다.
 * 배치(InventoryBatch) 기준의 단일 행 정보를 전달한다.</p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOutDetailDTO {

    /** 배치(로트) 번호 (inventory_batch_lot_no) */
    private String lotNo;

    /** 입고일 (inventory_batch_received_date) */
    private LocalDate receivedDate;

    /** 유통기한 (nullable, inventory_batch_expiration_date) */
    private LocalDate expirationDate;

    /** 입고 수량 (inventory_batch_received_quantity) */
    private BigDecimal receivedQuantity;

    /** 출고 후 배치 잔량 (inventory_batch_quantity) */
    private BigDecimal remainingQuantity;

    /** 단가 (배치별 매입가/공급가, inventory_batch_unit_price) */
    private BigDecimal unitPrice;
}
