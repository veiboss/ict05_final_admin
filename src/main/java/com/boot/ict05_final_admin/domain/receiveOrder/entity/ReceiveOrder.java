package com.boot.ict05_final_admin.domain.receiveOrder.entity;

import com.boot.ict05_final_admin.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 본사 수주 엔티티 (가맹점 발주 내역 기반)
 */
@Entity
@Table(name = "purchase_order")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiveOrder {

    /** 수주 시퀀스 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_order_id", columnDefinition = "INT UNSIGNED")
    private Long id;

    /** 가맹점 시퀀스 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id_fk", nullable = false)
    private Store store;

    /** 수주 번호 */
    @Column(name = "purchase_order_code", length = 32, nullable = false, unique = true)
    private String orderCode;

    /** 수주일 */
    @Column(name = "purchase_order_date", nullable = false)
    private LocalDate orderDate;

    /** 수주 총액 */
    @Column(name = "purchase_order_total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    /** 수주 비고 */
    @Column(name = "purchase_order_remark", columnDefinition = "TEXT")
    private String remark;

    /** 수주 공급업체명 */
    @Column(name = "purchase_order_supplier", length = 100, nullable = false)
    private String supplier;

    /** 수주 상태 */
    @Column(name = "purchase_order_status")
    @Enumerated(EnumType.STRING)
    private ReceiveOrderStatus status;

    /** 수주 우선순위 */
    @Column(name = "purchase_order_priority")
    @Enumerated(EnumType.STRING)
    private ReceiveOrderPriority priority;

    /** 수주 배송 예정일 */
    @Column(name = "purchase_order_delivery_date")
    private LocalDate deliveryDate;

    /** 수주 실제 납기일 */
    @Column(name = "purchase_order_actual_delivery_date")
    private LocalDate actualDeliveryDate;

}
