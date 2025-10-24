package com.boot.ict05_final_admin.domain.menu.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class MenuModifyFormDTO {

    /** 수정 대상 메뉴 ID */
    private Long menuId;

    /** 메뉴 카테고리 ID (필요시 함께 전달) */
    private Long menuCategoryId;

    /** 카테고리 */
    @NotNull
    private String menuCategoryName;

    /** 판매 상태 */
    @NotNull
    private Boolean menuShow;

    /** 메뉴코드 */
    @NotNull
    private String menuCode;

    /** 메뉴명 */
    @NotBlank
    private String menuName;

    /** 영문명  */
    private String menuNameEnglish;

    /** 가격 */
    @NotNull @DecimalMin("0.0")
    private BigDecimal menuPrice;

    /** 설명 */
    private String menuInformation;

    /** 칼로리(kcal) */
    @NotNull @Min(0)
    private Integer menuKcal;

    /** 재료구성 – 주재료 (menuRecipeId 포함됨) */
    @NotNull
    private List<RecipeItemDTO> mainMaterials;

    /** 재료구성 – 소스 (menuRecipeId 포함됨) */
    @NotNull
    private List<RecipeItemDTO> sauceMaterials;

}
