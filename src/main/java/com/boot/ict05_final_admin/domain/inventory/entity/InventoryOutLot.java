package com.boot.ict05_final_admin.domain.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 출고-로트 아이템 (InventoryOutLot)
 *
 * <p>한 건의 출고(헤더)와 여러 로트(배치)의 대응 관계를 관리한다.</p>
 * <ul>
 *   <li>본사 → 가맹점 출고: <code>store_id_fk</code>는 출고 헤더(InventoryOut)에 지정</li>
 *   <li>가맹점 내부 사용 또는 폐기 등: <code>store_id_fk</code>는 출고 헤더(InventoryOut)에 지정</li>
 *   <li>조인 엔티티, FIFO 결과 저장 단위, (out,batch) 유니크</li>
 *   <li>잔량 차감은 이 단위로만</li>
 * </ul>
 * <p>무결성</p>
 * <ul>
 *   <li>동일 출고 헤더에서 동일 로트 중복 금지</li>
 *   <li>아이템 수량의 합계 = 출고 헤더의 총 출고 수량</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inventory_out_lot",
        indexes = {
            @Index(name = "ix_outlot_out", columnList = "inventory_out_id_fk"),
            @Index(name = "ix_outlot_batch", columnList = "inventory_batch_id_fk")
        },
        uniqueConstraints = {
            // 한 출고에서 같은 배치를 여러 줄로 중복 기록하지 않도록 강제하고 싶으면 유지
            @UniqueConstraint(name = "uq_outlot_out_batch", columnNames = {"inventory_out_id_fk", "inventory_batch_id_fk"})
        }
)
public class InventoryOutLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_out_lot_id", columnDefinition = "BIGINT UNSIGNED")
    @Comment("출고-로트 행 시퀀스")
    private Long id;

    /** 출고 헤더 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_out_id_fk",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_outlot_out"))
    @Comment("출고 헤더 FK")
    private InventoryOut out;

    /** 배치(로트). 과거 미지정 복구용으로 NULL 허용 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_batch_id_fk",
            foreignKey = @ForeignKey(name = "fk_outlot_batch"))
    @Comment("배치(로트) FK")
    private InventoryBatch batch;

    /** 해당 로트에서 차감된 수량 */
    @Column(name = "quantity", precision = 15, scale = 3, nullable = false)
    @Comment("해당 로트에서 출고된 수량")
    private BigDecimal quantity;

    /** 생성 시각(옵션) */
    @Column(name = "created_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    @Comment("등록일시")
    private LocalDateTime createdAt;
}
