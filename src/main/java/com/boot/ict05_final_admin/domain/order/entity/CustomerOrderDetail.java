package com.boot.ict05_final_admin.domain.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

/**
 * 주문 상세(CustomerOrderDetail) 엔티티
 *
 * <p>주문에 포함된 개별 메뉴/수량/금액 정보를 담는다.</p>
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "customer_order_detail")
public class CustomerOrderDetail {

    /** 주문 상세 시퀀스 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_order_detail_id")
    private Long id;

    /** 주문(FK) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_order_id_fk", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Setter
    private CustomerOrder order;

    /** 메뉴 시퀀스(FK) - 아직 Menu 엔티티가 없으므로 Long 보관 */
    @Column(name = "menu_id_fk", nullable = false)
    private Long menuIdFk;

    /** 주문 수량 */
    @Column(name = "customer_order_detail_quantity", nullable = false)
    private Integer quantity;

    /** 주문 금액(해당 라인 총액) */
    @Column(name = "customer_order_detail_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal lineTotal;

    @PrePersist
    void prePersist() {
        if (quantity == null) quantity = 1;
        if (lineTotal == null) lineTotal = BigDecimal.ZERO;
    }
}