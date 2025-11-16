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
 * 입고 이력 (InventoryIn)
 *
 * <p>본사 및 가맹점의 입고 내역을 관리한다.</p>
 */
@Entity
@Table(name = "inventory_in")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_in_id", columnDefinition = "BIGINT UNSIGNED")
    @Comment("입고 시퀀스")
    private Long id;

    /** 재료 (FK: material.material_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id_fk",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_inventory_in_material"))
    private Material material;

    /** 가맹점 (선택, 본사 입고는 NULL) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id_fk",
            foreignKey = @ForeignKey(name = "fk_inventory_in_store"))
    @Comment("가맹점 코드 (fk)")
    private Store store;

    /** 입고 수량 */
    @Column(name = "inventory_in_quantity", precision = 15, scale = 3, nullable = false,
            columnDefinition = "DECIMAL(15,3) DEFAULT 0")
    @Comment("입고 수량")
    private BigDecimal quantity;

    /** 입고 후 재고량 */
    @Column(name = "inventory_in_stock_after", precision = 15, scale = 3)
    private BigDecimal stockAfter;

    /** 입고 단가 (본사 매입가) */
    @Column(name = "inventory_in_unit_price", precision = 15, scale = 2, nullable = false,
            columnDefinition = "BIGINT")
    @Comment("입고 단가(본사 매입가)")
    private BigDecimal unitPrice;

    /** 출고 단가 (가맹점 공급가) */
    @Column(name = "inventory_in_selling_price", precision = 15, scale = 2,
            columnDefinition = "BIGINT")
    @Comment("출고 단가(가맹점 공급가)")
    private BigDecimal sellingPrice;

    @Column(name = "inventory_lot", length = 32, unique = true)
    @Comment("로트 번호")
    private String lotNo;

    /** 입고일시(실제 입고일) */
    @Column(name = "inventory_in_date", nullable = false,
            columnDefinition = "DATETIME")
    @Comment("입고일시")
    private LocalDateTime inDate;

    /** 비고 */
    @Column(name = "inventory_in_memo", columnDefinition = "VARCHAR(255)")
    @Comment("비고")
    private String memo;

    /** 등록일시 (자동 생성) */
    @CreationTimestamp
    @Column(name = "inventory_in_created_at",
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    @Comment("등록일 (자동 생성)")
    private LocalDateTime createdAt;


    @Comment("트랜잭션 상태(DRAFT/CONFIRMED/CANCELLED/REVERSED)")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InventoryRecordStatus status = InventoryRecordStatus.CONFIRMED;
}
