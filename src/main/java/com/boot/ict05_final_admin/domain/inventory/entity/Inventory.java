package com.boot.ict05_final_admin.domain.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;


/**
 * 본사 재고(Inventory) 엔티티.
 */
@Entity
@Table(name = "inventory",
        uniqueConstraints = @UniqueConstraint(name = "uq_inv_material", columnNames = "material_id_fk")
)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@SuperBuilder
@Comment("본사 재고")
public class Inventory extends InventoryBase {

    /** 재고 시퀀스 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id", columnDefinition = "BIGINT UNSIGNED")
    @Comment("재고 ID")
    protected Long id;

    /** 본사 재료 (FK: material.material_id) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id_fk", nullable = false,
            foreignKey = @ForeignKey(name = "fk_inventory_material"))
    @Comment("재료")
    protected Material material;
}
