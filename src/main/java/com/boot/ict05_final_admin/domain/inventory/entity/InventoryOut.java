package com.boot.ict05_final_admin.domain.inventory.entity;

import com.boot.ict05_final_admin.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;
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

    /** 출고 수량 (LOT 합계) */
    @Column(name = "inventory_out_quantity", precision = 15, scale = 3, nullable = false,
            columnDefinition = "DECIMAL(15,3) DEFAULT 0")
    @Comment("출고 수량")
    private BigDecimal quantity;

    /** 출고 후 재고량 */
    @Setter
    @Column(name = "inventory_out_stock_after", precision = 15, scale = 3)
    private BigDecimal stockAfter;

    /** 출고 단가 (가맹점 공급가 또는 판매가) */
    @Column(name = "inventory_out_unit_price", nullable = false,
            columnDefinition = "BIGINT")
    @Comment("출고 단가(가맹점 공급가 또는 판매가)")
    private BigDecimal unitPrice;

    /** 로트 아이템들 */
    @OneToMany(mappedBy = "out", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private java.util.List<InventoryOutLot> lotItems = new java.util.ArrayList<>();

    /** 비고 */
    @Column(name = "inventory_out_memo", columnDefinition = "VARCHAR(255)")
    @Comment("비고")
    private String memo;

    /** 출고일시 */
    @Column(name = "inventory_out_date", nullable = false,
            columnDefinition = "DATETIME")
    @Comment("출고일시")
    private LocalDateTime outDate;

    /** 등록일시 (자동 생성) */
    @CreationTimestamp
    @Column(name = "inventory_out_created_at",
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    @Comment("등록일시 (자동 생성)")
    private LocalDateTime createdAt;

    @Comment("트랜잭션 상태(DRAFT/CONFIRMED/CANCELLED/REVERSED)")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InventoryRecordStatus status = InventoryRecordStatus.CONFIRMED;

    @Comment("리버설 대상 출고 헤더 ID(원본 출고)")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_for_id")
    private InventoryOut reversalFor;

    /**
     * 로트 아이템 추가 편의 메서드
     *
     * <p>입력된 {@link InventoryOutLot} 객체를 현재 출고 헤더(THIS)와 연관시키고
     * 출고-로트 아이템 컬렉션에 추가합니다. 영속성 전파(Cascade.ALL) 환경에서
     * 헤더 저장 시 아이템도 함께 저장됩니다.
     * 헤더 1건 + 로트 아이템 N건</p>
     *
     * @param item 현재 출고에 연결할 로트 아이템
     * @throws IllegalArgumentException item이 {@code null} 인 경우
     */
    public void addLotItem(InventoryOutLot item) {
        if (item == null) {
            throw new IllegalArgumentException("item must not be null");
        }
        item.setOut(this);
        this.lotItems.add(item);
    }

    /**
     * 특정 로트 수량만큼 출고 수량 합계를 감소시킨다.
     *
     * @param quantity 차감할 수량 (null 허용 안 함)
     */
    public void decreaseQuantity(BigDecimal quantity) {
        if (quantity == null) {
            return;
        }
        if (this.quantity == null) {
            this.quantity = BigDecimal.ZERO;
        }

        this.quantity = this.quantity.subtract(quantity);
        if (this.quantity.compareTo(BigDecimal.ZERO) < 0) {
            this.quantity = BigDecimal.ZERO;
        }
    }
}