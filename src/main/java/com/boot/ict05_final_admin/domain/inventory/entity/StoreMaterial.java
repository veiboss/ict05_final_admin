package com.boot.ict05_final_admin.domain.inventory.entity;

import com.boot.ict05_final_admin.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 가맹점 재료(StoreMaterial) 엔티티
 *
 * <p>각 가맹점의 재료 정보를 관리한다.</p>
 * <p>본사에서 공급받은 재료(isHqMaterial = true)와
 * 가맹점 자체 등록 재료(isHqMaterial = false)를 모두 포함한다.</p>
 */
@Entity
@Table(name = "store_material",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id_fk", "store_material_code"})
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreMaterial {

    /** 가맹점 재료 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_material_id", columnDefinition = "BIGINT UNSIGNED COMMENT '가맹점 재료 시퀀스'")
    private Long id;

    /** 가맹점 (FK: store.store_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "store_id_fk",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_sm_store"),
            columnDefinition = "BIGINT UNSIGNED COMMENT '매장 시퀀스 (FK)'")
    private Store store;

    /** 본사 재료 (FK: material.material_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id_fk", nullable = true,
            foreignKey = @ForeignKey(name = "fk_sm_material"),
            columnDefinition = "BIGINT UNSIGNED COMMENT '본사 재료 (FK)'")
    private Material material;

    /** 가맹점 재료 코드 (점포별 고유) */
    @Column(name = "store_material_code", length = 30, nullable = false,
            columnDefinition = "VARCHAR(30) COMMENT '가맹점 재료 코드(점포별 고유)'")
    private String code;

    /** 가맹점 재료명 */
    @Column(name = "store_material_name", length = 100, nullable = false,
            columnDefinition = "VARCHAR(100) COMMENT '가맹점 재료명'")
    private String name;

    /** 카테고리 */
    @Column(name = "store_material_category", length = 50,
            columnDefinition = "VARCHAR(50) COMMENT '가맹점 재료 카테고리'")
    private String category;

    /** 단위 (예: kg, 개, L 등) */
    @Column(name = "store_material_unit", length = 20,
            columnDefinition = "VARCHAR(20) COMMENT '재료 단위'")
    private String unit;

    /** 공급업체명 */
    @Column(name = "store_material_supplier", length = 100,
            columnDefinition = "VARCHAR(100) COMMENT '가맹점 재료 공급업체명'")
    private String supplier;

    /** 보관온도 */
    @Enumerated(EnumType.STRING)
    @Column(name = "store_material_temperature",
            columnDefinition = "ENUM('TEMPERATURE','REFRIGERATE','FREEZE') COMMENT '보관온도'")
    private MaterialTemperature temperature;

    /** 재료 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "store_material_status", nullable = false,
            columnDefinition = "ENUM('USE','STOP') DEFAULT 'USE' COMMENT '재료 상태'")
    private MaterialStatus status;

    /** 현재 수량 */
    @Column(name = "store_material_quantity", precision = 15, scale = 3, nullable = false,
            columnDefinition = "DECIMAL(15,3) DEFAULT 0.000 COMMENT '현재 수량'")
    private BigDecimal quantity;

    /** 적정 수량 */
    @Column(name = "store_material_optimal_quantity", precision = 15, scale = 3,
            columnDefinition = "DECIMAL(15,3) COMMENT '적정 수량'")
    private BigDecimal optimalQuantity;

    /** 매입가 */
    @Column(name = "store_material_purchase_price", precision = 10, scale = 2,
            columnDefinition = "DECIMAL(10,2) COMMENT '매입가'")
    private BigDecimal purchasePrice;

    /** 판매가 */
    @Column(name = "store_material_selling_price", precision = 10, scale = 2,
            columnDefinition = "DECIMAL(10,2) COMMENT '판매가'")
    private BigDecimal sellingPrice;

    /** 유통기한 */
    @Column(name = "store_material_expiration_date", columnDefinition = "DATE COMMENT '유통기한'")
    private LocalDate expirationDate;

    /** 본사 재료 여부 (1=본사, 0=가맹점 자체 등록) */
    @Column(name = "store_material_is_hq_material", nullable = false,
            columnDefinition = "TINYINT(1) DEFAULT 0 COMMENT '본사 재료 여부'")
    private boolean isHqMaterial;

    /** 등록일 */
    @Column(name = "store_material_reg_date", nullable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일'")
    private LocalDateTime regDate;

    /** 수정일 */
    @Column(name = "store_material_modify_date", nullable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일'")
    private LocalDateTime modifyDate;
}