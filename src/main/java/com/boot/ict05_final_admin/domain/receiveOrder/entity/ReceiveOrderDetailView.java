package com.boot.ict05_final_admin.domain.receiveOrder.entity;

import com.boot.ict05_final_admin.domain.inventory.entity.HqInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.math.BigDecimal;

@Entity(name = "ReceiveOrderDetailView")
@Getter
@NoArgsConstructor
@Immutable
@Subselect("SELECT * FROM receive_order_detail")
public class ReceiveOrderDetailView {

    @Id
    @Column(name = "purchase_order_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id_fk", insertable = false, updatable = false)
    private ReceiveOrderView receiveOrder;  // ← 반드시 참조 타입

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id_fk", insertable = false, updatable = false)
    private HqInventory hqInventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id_fk", insertable = false, updatable = false)
    private Material material;

    @Column(name = "purchase_order_detail_unit_price", insertable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "purchase_order_detail_count", insertable = false, updatable = false)
    private Integer count;

    @Column(name = "purchase_order_detail_total_price", insertable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;
}
