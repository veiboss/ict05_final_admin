package com.boot.ict05_final_admin.domain.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 입고 등록 DTO (본사/가맹점 공용)
 */
@Data
public class InventoryInWriteDTO {

    /** 재고 ID (HqInventory 기준) */
    private Long inventoryId;

    /** 입고 수량 */
    private BigDecimal quantity;

    /** 입고 단가 (본사 매입가) */
    private Long unitPrice;

    /** 출고 단가 (가맹점 공급가) */
    private Long sellingPrice;

    /** 입고일 */
    private LocalDate inDate;

    /** 비고 */
    private String memo;

    /** 가맹점 ID (선택) */
    private Long storeId;
}
