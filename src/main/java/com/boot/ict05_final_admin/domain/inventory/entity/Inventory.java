package com.boot.ict05_final_admin.domain.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 재고(Inventory) 공통 추상 클래스.
 *
 * <p>본사(HqInventory), 가맹점(StoreInventory)의 공통 필드를 정의한다.</p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class Inventory {

    /** 현재 재고 수량 */
    @Column(name = "inventory_quantity", nullable = false,
            columnDefinition = "INT DEFAULT 0 COMMENT '현재 재고 수량'")
    protected Integer quantity;

    /** 적정 재고 수량 */
    @Column(name = "inventory_optimal_quantity",
            columnDefinition = "INT COMMENT '적정 재고 수량'")
    protected Integer optimalQuantity;

    /** 재고 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_status", nullable = false,
            columnDefinition = "ENUM('SUFFICIENT','LOW','SHORTAGE') DEFAULT 'SUFFICIENT' COMMENT '재고 상태'")
    protected InventoryStatus status;

    /** 마지막 업데이트 일시 */
    @Column(name = "inventory_update_date", nullable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '재고 수정일'")
    protected LocalDateTime updateDate;
}
