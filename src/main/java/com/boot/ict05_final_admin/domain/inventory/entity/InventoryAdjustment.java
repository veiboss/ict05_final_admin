package com.boot.ict05_final_admin.domain.inventory.entity;

import jakarta.persistence.*;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 본사 재고 수량 조정 이력
 *
 * <p>입출고 외의 이유(분실, 파손, 오입력 등)로 인한 재고 수정 시 발생하는 로그를 기록한다.</p>
 *
 * <p>이 데이터는 v_inventory_log 뷰와 UNION 되어 입출고 내역과 함께 조회된다.</p>
 *
 * @author ICT
 * @since 2025.10
 */
@Entity
@Table(name = "inventory_adjustment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adjustment_id", columnDefinition = "BIGINT COMMENT '조정 시퀀스'")
    private Long id;

    /** 본사 재고 FK */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id_fk", nullable = false)
    @Comment("본사 재고 FK")
    private HqInventory inventory;

    /** 조정 전 수량 */
    @Column(name = "inventory_adjustment_quantity_before", precision = 15, scale = 3, nullable = false)
    @Comment("조정 전 수량")
    private BigDecimal quantityBefore;

    /** 조정 후 수량 */
    @Column(name = "inventory_adjustment_quantity_after", precision = 15, scale = 3, nullable = false)
    @Comment("조정 후 수량")
    private BigDecimal quantityAfter;

    /** 증감 수량 (후 - 전) */
    @Column(name = "inventory_adjustment_difference", precision = 15, scale = 3, nullable = false)
    @Comment("증감 수량")
    private BigDecimal difference;

    /** 조정 단가 */
    @Column(name = "inventory_adjustment_unit_price", columnDefinition = "BIGINT")
    @Comment("조정 단가")
    private Long unitPrice;

    /** 비고 / 조정 사유 */
    @Column(name = "inventory_adjustment_memo", columnDefinition = "VARCHAR(255)")
    @Comment("비고 / 조정 사유")
    private String memo;

    /** 조정일시 */
    @CreationTimestamp
    @Column(name = "inventory_adjustment_created_at", nullable = false, updatable = false)
    @Comment("조정일시")
    private LocalDateTime createdAt;

    /** 조정 사유 분류 */
    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_adjustment_reason", length = 20)
    @Comment("조정 사유 (MANUAL, DAMAGE, LOSS 등)")
    private AdjustmentReason reason;

    /**
     * 수량 증감 계산 헬퍼
     */
    public void calculateDifference() {
        if (quantityBefore != null && quantityAfter != null) {
            this.difference = quantityAfter.subtract(quantityBefore);
        }
    }

    /**
     * 조정 사유 Enum
     */
    public enum AdjustmentReason {
        MANUAL,     // 수동 수정
        DAMAGE,     // 파손
        LOSS,       // 분실
        ERROR       // 데이터 오류 정정
    }
}
