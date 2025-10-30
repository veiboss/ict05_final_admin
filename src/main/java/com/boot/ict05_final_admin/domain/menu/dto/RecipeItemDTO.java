package com.boot.ict05_final_admin.domain.menu.dto;

import com.boot.ict05_final_admin.domain.menu.entity.RecipeUnit;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter @Setter
@NoArgsConstructor
public class RecipeItemDTO {

    private Long materialId;

    /** 레시피 항목명 — ‘체다치즈’, ‘소금’, ‘소스 베이스’ */
    private String itemName;

    /** 수량 */
    @DecimalMin(value = "0.0", inclusive = false, message = "수량은 0보다 커야 합니다")
    private BigDecimal recipeQty;

    /** 단위 (g/ml/개/장) */
    private RecipeUnit recipeUnit;

    /** 표시 순서 (자동 세팅) */
    private Integer recipeSortNo;

}
