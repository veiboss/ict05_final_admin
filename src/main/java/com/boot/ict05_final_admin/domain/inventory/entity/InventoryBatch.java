package com.boot.ict05_final_admin.domain.inventory.entity;

import com.boot.ict05_final_admin.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 재고 배치(입고분, LOT) 엔티티 클래스
 *
 * <p>본 클래스는 {@code inventory_batch} 테이블과 매핑되며,
 * 각 재료의 입고 단위(LOT)별 수량, 입고일, 유통기한을 관리합니다.</p>
 *
 * <p>입고 시마다 한 건씩 생성되며,
 * 출고 또는 소진 시 {@link #quantity} 값이 감소합니다.</p>
 *
 * <p>잔량은 InventoryOutLot/Adjustment 경로로만 변경. 직접 업데이트 금지</p>
 *
 * <ul>
 *     <li>store: 본사 또는 가맹점 (재고 소유 주체)</li>
 *     <li>material: 재료</li>
 *     <li>receivedDate: 입고일</li>
 *     <li>expirationDate: 유통기한</li>
 *     <li>quantity: 남은 수량</li>
 * </ul>
 *
 * @author 김주연
 * @since 2025-11-05
 */
@Entity
@Table(name = "inventory_batch")
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryBatch {

    /** 재고 배치 시퀀스 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_batch_id", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    @Comment("재고 배치 시퀀스")
    private Long id;

    /** 로트 번호 */
    @Column(name = "inventory_batch_lot_no", length = 32, nullable = false, unique = true)
    @Comment("로트 번호")
    private String lotNo;

    /** 가맹점 또는 본사 (소유 주체) */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id_fk", columnDefinition="BIGINT UNSIGNED",
            foreignKey = @ForeignKey(name = "fk_si_store"))
    @Comment("가맹점(FK) 또는 본사")
    private Store store;

    /** 재료 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id_fk", referencedColumnName = "material_id", nullable = false)
    @Comment("재료 시퀀스 (FK)")
    private Material material;

    /** 입고일 (실제 입고 시각) */
    @Column(name = "inventory_batch_received_date", nullable = false, columnDefinition = "DATETIME")
    @Comment("입고일시")
    @Builder.Default
    private LocalDateTime receivedDate = LocalDateTime.now();

    /** 입고 단가 (본사 매입가) */
    @Column(name = "inventory_batch_unit_price", precision = 15, scale = 2, nullable = false)
    @Comment("해당 로트의 입고 단가(본사 매입가)")
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    /** 유통기한 */
    @Column(name = "inventory_batch_expiration_date", nullable = false, columnDefinition = "DATE")
    @Comment("유통기한")
    private LocalDate expirationDate;

    /** 입고 당시 수량 */
    @Column(name = "inventory_batch_received_quantity", precision = 15, scale = 3, nullable = false,
            columnDefinition = "DECIMAL(15,3) DEFAULT 0")
    @Comment("입고 당시 수량")
    @Builder.Default
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    /** 현재 남은 수량 */
    @Column(name = "inventory_batch_quantity", precision = 15, scale = 3, nullable = false,
            columnDefinition = "DECIMAL(15,3) DEFAULT 0")
    @Comment("현재 남은 수량")
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ZERO;

    /** 등록일시 (자동 생성) */
    @CreationTimestamp
    @Column(name = "inventory_batch_created_at",
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    @Comment("등록일시 (자동 생성)")
    private LocalDateTime createdAt;

    /**
     * 남은 수량을 차감한다.
     *
     * @param amount 차감할 수량
     */
    public void subtractQuantity(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) return;
        if (this.quantity == null) {
            this.quantity = BigDecimal.ZERO;
        }
        BigDecimal after = this.quantity.subtract(amount);
        if (after.signum() < 0) {
            throw new IllegalArgumentException("배치 잔량 부족: batchId=" + id +
                    ", 현재=" + this.quantity + ", 요청=" + amount);
        }
        this.quantity = after;
    }
}
