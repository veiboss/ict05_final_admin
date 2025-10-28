package com.boot.ict05_final_admin.domain.menu.dto;

import com.boot.ict05_final_admin.domain.menu.entity.RecipeUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter @Setter
public class RecipeItemDTO {

    @NotNull(message = "재료를 선택하세요")
    private Long materialId;

    /** 항목명(직접 입력) */
    @NotBlank(message = "항목명을 입력하세요")
    private String itemName;

    /** 수량 */
    @NotNull(message = "수량을 입력하세요")
    @DecimalMin(value = "0.0", inclusive = false, message = "수량은 0보다 커야 합니다")
    private BigDecimal recipeQty;

    /** 단위 (g/ml/개/장) */
    @NotNull(message = "단위를 선택하세요")
    private RecipeUnit recipeUnit;

    /** 표시 순서 (자동 세팅) */
    private Integer recipeSortNo;
}
