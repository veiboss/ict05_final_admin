package com.boot.ict05_final_admin.domain.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 본사 재고(HqInventory) 엔티티.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "inventory")
public class HqInventory extends Inventory {

    /** 재고 시퀀스 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id", columnDefinition = "BIGINT UNSIGNED COMMENT '재고 시퀀스'")
    private Long id;

    /** 본사 재료 (FK: material.material_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id_fk", nullable = false,
            foreignKey = @ForeignKey(name = "fk_inventory_material"))
    private Material material;

    public void updateStatus() {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = InventoryStatus.SHORTAGE;
        } else if (quantity.compareTo(optimalQuantity) < 0) {
            this.status = InventoryStatus.LOW;
        } else {
            this.status = InventoryStatus.SUFFICIENT;
        }
    }
}
