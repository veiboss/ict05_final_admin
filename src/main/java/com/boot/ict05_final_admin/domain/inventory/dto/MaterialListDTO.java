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
    private String title;

    /** 재료 카테고리 */
    private MaterialCategory materialCategory;
    
    /** 재료 단위 */
    private String unit;

    /** 공급업체명 */
    private String supplier;

    /** 재료 보관온도 */
    private MaterialTemperature materialTemperature;

    /** 재료 상태*/
    private MaterialStatus materialStatus;
}
