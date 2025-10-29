package com.boot.ict05_final_admin.domain.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 본사 입출고 통합 뷰 엔티티
 *
 * <p>DB View(v_inventory_log)를 매핑하여
 * 입출고 내역을 단일 테이블처럼 조회할 수 있게 한다.</p>
 *
 * @author ICT
 * @since 2025.10
 */
@Entity
@Table(name = "v_inventory_log")
@Getter
@NoArgsConstructor
@Immutable // 읽기 전용
public class InventoryLogView {

    /** 통합 로그 ID (입고 or 출고 시퀀스) */
    @Id
    @Column(name = "log_id")
    private Long id;

    /** 재료 ID */
    @Column(name = "material_id")
    private Long materialId;

    /** 입출고 구분 (INCOME / OUTGO) */
    @Column(name = "log_type")
    private String type;

    /** 입출고 날짜 */
    @Column(name = "log_date")
    private LocalDateTime date;

    /** 수량 */
    @Column(name = "quantity")
    private BigDecimal quantity;

    /** 단가 */
    @Column(name = "unit_price")
    private Long unitPrice;

    /** 비고 */
    @Column(name = "memo")
    private String memo;

    /** 가맹점 FK */
    @Column(name = "store_id")
    private Long storeId;

    /** 재고 */
    @Column(name = "stock_after", precision = 15, scale = 3)
    private BigDecimal stockAfter;
}
