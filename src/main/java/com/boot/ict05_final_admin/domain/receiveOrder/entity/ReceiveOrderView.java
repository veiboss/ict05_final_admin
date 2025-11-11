package com.boot.ict05_final_admin.domain.receiveOrder.entity;

import com.boot.ict05_final_admin.domain.store.entity.Store;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "ReceiveOrderView")
@Getter
@NoArgsConstructor
@Immutable
@Subselect("SELECT * FROM receive_order")
public class ReceiveOrderView {

    @Id
    @Column(name = "purchase_order_id") // VIEW에 존재하는 고유키
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id_fk", insertable = false, updatable = false)
    private Store store;

    @Column(name = "purchase_order_code", insertable = false, updatable = false)
    private String orderCode;

    @Column(name = "purchase_order_date", insertable = false, updatable = false)
    private LocalDate orderDate;

    @Column(name = "purchase_order_total_price", insertable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "purchase_order_total_count", insertable = false, updatable = false)
    private Integer totalCount;

    @Column(name = "purchase_order_remark", insertable = false, updatable = false, columnDefinition = "TEXT")
    private String remark;

    @Column(name = "purchase_order_supplier", insertable = false, updatable = false, length = 100)
    private String supplier;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_order_status", insertable = false, updatable = false)
    private ReceiveOrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_order_priority", insertable = false, updatable = false)
    private ReceiveOrderPriority priority;

    @Column(name = "purchase_order_delivery_date", insertable = false, updatable = false)
    private LocalDate deliveryDate;

    @Column(name = "purchase_order_actual_delivery_date", insertable = false, updatable = false)
    private LocalDate actualDeliveryDate;

    // VIEW에서 쓰기 경로 열릴 수 있으니 컬렉션 제거
    // private List<ReceiveOrderDetail> details;  // 제거
}
