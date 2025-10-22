package com.boot.ict05_final_admin.domain.menu.dto;

import com.boot.ict05_final_admin.domain.menu.entity.MenuCategoryEnum;
import com.boot.ict05_final_admin.domain.menu.entity.MenuShowEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data       // getter, setter, toString 등 기본 메서드
@AllArgsConstructor     // 매개변수 생성자
@NoArgsConstructor      // 디폴트 생성자
public class MenuListDTO {

    /** 메뉴 시퀀스 */
    private Long menuId;

    /** 판매상태*/
    private MenuShowEnum menuShow;

    /** 메뉴명*/
    private String menuName;

    /** 카테고리명 */
    private MenuCategoryEnum menuCategoryName;

    /** 주재료 리스트 */
    private List<String> materialNames;

    /** 가격 */
    private BigDecimal menuPrice;

    /** 메뉴 칼로리 */
    private int menuKcal;

    /** 한글 라벨: 상태 */
    public String getMenuShowLabel() {
        return menuShow != null ? menuShow.getDescription() : "";
    }

    /** 한글 라벨 : 카테고리 */
    public String getMenuCategoryLabel() {
        return menuCategoryName != null ? menuCategoryName.getDescription() : "";
    }

    /** 화면에서 문자열로 쓸 때 */
    public String getMaterialNamesJoined() {
        return materialNames != null ? String.join(", ", materialNames) : "";
    }
}