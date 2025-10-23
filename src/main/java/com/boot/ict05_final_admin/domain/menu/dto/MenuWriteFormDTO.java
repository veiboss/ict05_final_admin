package com.boot.ict05_final_admin.domain.menu.dto;

import com.boot.ict05_final_admin.domain.menu.entity.MenuCategoryEnum;
import com.boot.ict05_final_admin.domain.menu.entity.MenuShowEnum;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuWriteFormDTO {

    /** 카테고리(필수) */
    @NotNull(message = "카테고리를 선택해주세요")
    private MenuCategoryEnum menuCategoryName;

    /** 판매 상태(필수) */
    @NotNull(message = "판매상태를 선택해주세요")
    private MenuShowEnum menuShow;

    /** 메뉴명(필수) */
    @NotBlank
    @Size(max = 100)
    private String menuName;

    /** 영문명(필수) */
    @NotBlank
    @Size(max = 150)
    private String menuNameEnglish;

    /** 가격(필수) */
    @NotNull @DecimalMin("0.0")
    private BigDecimal menuPrice;

    /** 설명(필수) */
    @NotNull
    private String menuInformation;

    /** 칼로리(kcal)(필수) */
    @NotNull @Min(0)
    private Integer menuKcal;

    /** 재료구성 – 주재료(필수) */
    @NotNull
    private List<RecipeItemDTO> mainMaterials;

    /** 재료구성 – 소스(필수) */
    @NotNull
    private List<RecipeItemDTO> sauceMaterials;
}
