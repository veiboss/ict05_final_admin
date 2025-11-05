package com.boot.ict05_final_admin.domain.inventory.entity;

import com.boot.ict05_final_admin.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 출고 이력 (InventoryOut)
 *
 * <p>본사 및 가맹점의 출고 내역을 관리한다.</p>
 * <ul>
 *   <li>본사 → 가맹점 출고: store_id_fk 지정</li>
 *   <li>가맹점 내부 사용 또는 폐기 등: store_id_fk 지정</li>
 * </ul>
 */
@Entity
@Table(name = "inventory_out")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_out_id", columnDefinition = "BIGINT UNSIGNED")
    @Comment("출고 시퀀스")
    private Long id;

    /** 재료 (FK: material.material_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id_fk",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_inventory_out_material"))
    private Material material;

    /** 가맹점 (선택, 본사 출고는 NULL) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id_fk",
            foreignKey = @ForeignKey(name = "fk_inventory_out_store"))
    private Store store;

    /** 출고 수량 */
    @Column(name = "inventory_out_quantity", precision = 15, scale = 3, nullable = false,
            columnDefinition = "DECIMAL(15,3) DEFAULT 0")
    @Comment("출고 수량")
    private BigDecimal quantity;

    /** 출고 후 재고량 */
    @Column(name = "inventory_out_stock_after", precision = 15, scale = 3)
    private BigDecimal stockAfter;

    /** 출고 단가 (가맹점 공급가 또는 판매가) */
    @Column(name = "inventory_out_unit_price", nullable = false,
            columnDefinition = "BIGINT")
    @Comment("출고 단가(가맹점 공급가 또는 판매가)")
    private BigDecimal unitPrice;

    /** 출고일시 */
    @Column(name = "inventory_out_date", nullable = false,
            columnDefinition = "DATETIME")
    @Comment("출고일시")
    private LocalDateTime outDate;

    /** 비고 */
    @Column(name = "inventory_out_memo", columnDefinition = "VARCHAR(255)")
    @Comment("비고")
    private String memo;

    /** 등록일시 (자동 생성) */
    @CreationTimestamp
    @Column(name = "inventory_out_created_at",
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    @Comment("등록일시 (자동 생성)")
    private LocalDateTime createdAt;
}