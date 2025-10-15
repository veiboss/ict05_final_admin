package com.boot.ict05_final_admin.domain.receiveOrder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 본사 수주 상세 엔티티 (가맹점 발주 상세 내역 기반)
 */
@Entity
@Table(name = "purchase_order_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiveOrderDetail {

    /** 수주 상세 시퀀스 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_order_detail_id")
    private Long detailId;

    /** 수주 시퀀스 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id_fk", nullable = false)
    private ReceiveOrder receiveOrder;

    /** 재료 재고 시퀀스 */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "material_inventory_id_fk", nullable = false)
//    private MaterialInventory MaterialInventory;

    /** 수주 단가 */
    @Column(name = "purchase_order_detail_unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal detailUnitPrice;

    /** 수주 수량 */
    @Column(name = "purchase_order_detail_count", nullable = false)
    private Integer detailCount;

    /** 수주 단가 총액 */
    @Column(name = "purchase_order_detail_total_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal detailTotalPrice;

}
