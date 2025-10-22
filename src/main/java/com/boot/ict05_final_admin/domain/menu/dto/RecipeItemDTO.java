package com.boot.ict05_final_admin.domain.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeItemDTO {

    /** 수정 화면에서 필요 */
    private Long menuRecipeId;

    /** 재료 식별자 (필수) */
    @NotNull
    private Long materialId;

    /** 표기용(선택) – 자동완성/미리보기용 */
    private String materialName;

    /** 수량 (문자열로 받아서 빈값/정수/소수 모두 처리) */
    @NotBlank
    private String recipeQty;

    /** 단위 (g, ml, 개 등) */
    @NotBlank
    private String recipeUnit;

    /** 표시 순서(1부터) */
    @NotNull @Positive
    private Integer recipeSortNo;

    /** 역할: MAIN / SAUCE */
    @NotBlank
    private String recipeRole;

    /** 재료 기본단위(표시/기본값 도움용, 선택) */
    private String materialBaseUnit;
}
