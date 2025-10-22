package com.boot.ict05_final_admin.domain.menu.dto;

import com.boot.ict05_final_admin.domain.menu.entity.MenuCategoryEnum;
import com.boot.ict05_final_admin.domain.menu.entity.MenuShowEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MenuDetailDTO {

    /** 메뉴 고유 ID */
    private Long menuId;

    /** 메뉴 카테고리 */
    private MenuCategoryEnum menuCategoryName;

    /** 판매 상태 */
    private MenuShowEnum menuShow;

    /** 메뉴코드 */
    @NotNull
    private String menuCode;

    /** 메뉴명 */
    private String menuName;

    /** 영문명 */
    private String menuNameEnglish;

    /** 가격 */
    private BigDecimal menuPrice;

    /** 설명 */
    private String menuInformation;

    /** 재료구성 */
    private List<RecipeItemDTO> mainMaterials;  // 주재료 구성 테이블
    private List<RecipeItemDTO> sauceMaterials; // 소스 구성 테이블

}
