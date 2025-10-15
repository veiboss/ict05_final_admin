package com.boot.ict05_final_admin.domain.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 재료(Material) 엔티티 클래스
 *
 * <p>본 클래스는 재료 테이블과 매핑되며,
 * 재료의 재료코드, 재료명, 카테고리, 단위, 공급업체명, 재료보관온도,  재료상태 등의 정보를 포함합니다.</p>
 *
 * <p>엔티티는 생성, 조회, 수정 기능을 지원하며,
 * {link #updateMaterial(MaterialModifyFormDTO)} 메서드를 통해 상태를 변경할 수 있습니다.</p>
 *
 * @author 김주연
 * @since 2025-10-15
 */

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "material")
public class Material {

    /** 재료 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Long id;

    /** 재료코드 */
    @Column(name = "material_code")
    private String code;

    /** 재료명 */
    @Column(name = "material_name")
    private String title;

    /** 재료 카테고리 */
    @Column(name = "material_category")
    private String category;

    /** 재료 단위 */
    @Column(name = "material_unit")
    private String unit;

    /** 공급업체명 */
    @Column(name = "material_supplier")
    private String supplier;

    /** 재료 보관온도 */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_temperature")
    private MaterialTemperature materialTemperature;

    /** 재료 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_status")
    private MaterialStatus materialStatus;

}
