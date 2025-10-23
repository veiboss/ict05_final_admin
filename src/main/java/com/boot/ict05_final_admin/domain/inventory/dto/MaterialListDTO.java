package com.boot.ict05_final_admin.domain.inventory.dto;

import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialTemperature;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialListDTO {

    /** 재료 고유 ID */
    private Long id;

    /** 재료 CODE */
    private String code;

    /** 재료명 */
    private String name;

    /** 재료 카테고리 */
    private MaterialCategory materialCategory;
    
    /** 기본 단위 (소진 단위) */
    private String baseUnit;

    /** 판매 단위 */
    private String salesUnit;

    /** 판매단위 → 기본단위 변환비율 */
    private Integer conversionRate;

    /** 공급업체명 */
    private String supplier;

    /** 재료 보관온도 */
    private MaterialTemperature materialTemperature;

    /** 재료 상태*/
    private MaterialStatus materialStatus;

    public String getCategoryDescription() {
        return materialCategory != null ? materialCategory.getDescription() : "";
    }
}
