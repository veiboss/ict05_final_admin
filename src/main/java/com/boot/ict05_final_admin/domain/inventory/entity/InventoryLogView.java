package com.boot.ict05_final_admin.domain.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * v_inventory_log 뷰 매핑 엔티티
 *
 * <p>주의: 읽기 전용(View) 엔티티. INSERT/UPDATE 금지.</p>
 */
@Entity
@Table(name = "v_inventory_log")
@Immutable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLogView {

    /** 뷰 전용 PK (ROW_NUMBER()) */
    @Id
    @Column(name = "row_id")
    private Long rowId;

    /** 업무용 로그 ID (입고/출고/조정 원본 PK) */
    @Column(name = "log_id")
    private Long logId;

    /** 로그 일시 */
    @Column(name = "log_date")
    private LocalDateTime date;

    /** 재료 ID */
    @Column(name = "material_id")
    private Long materialId;

    /** 메모 */
    @Column(name = "memo")
    private String memo;

    /** 수량(+입고, -출고, ±조정) */
    @Column(name = "quantity")
    private BigDecimal quantity;

    /** 로그 반영 후 재고 */
    @Column(name = "stock_after")
    private BigDecimal stockAfter;

    /** 가맹점 ID (nullable) */
    @Column(name = "store_id")
    private Long storeId;

    /** 로그 타입: INCOME / OUTGO / ADJUST */
    @Column(name = "log_type")
    private String type;

    /** 단가 (nullable) */
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    /** LOT 상세용 배치 PK (입고만 세팅) */
    @Column(name = "batch_id")
    private Long batchId;

    /** 가맹점명 (nullable) */
    @Column(name = "store_name")
    private String storeName;
}
