package com.boot.ict05_final_admin.domain.receiveOrder.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.Inventory;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReceiveOrderItemDTO {

    /** 주문 상품명 */
    public String name;

    /** 주문 상품 카테고리 */
    public MaterialCategory materialCategory;

    /** 주문 상품 수량 */
    public Integer detailCount;

    /** 주문 상품 단가 */
    public BigDecimal detailUnitPrice;

    /** 주문 상품 총액 (수량*단가) */
    public BigDecimal detailTotalPrice;

    /** 주문 상품 재고상태 */
    public InventoryStatus inventoryStatus;

    // 재고 상태 한글 값
    public String getInventoryStatusDescription() {
        return inventoryStatus != null ? inventoryStatus.getDescription() : "";
    }

}
