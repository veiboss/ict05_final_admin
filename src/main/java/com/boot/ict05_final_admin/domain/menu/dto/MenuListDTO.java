package com.boot.ict05_final_admin.domain.menu.dto;

import com.boot.ict05_final_admin.domain.menu.entity.MenuCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data       // getter, setter, toString 등 기본 메서드
@AllArgsConstructor     // 매개변수 생성자
@NoArgsConstructor      // 디폴트 생성자
public class MenuListDTO {

    /** 메뉴 시퀀스 */
    private  Long menuId;

    /** 메뉴명*/
    private String menuName;

    /** 메뉴 코드 */
    private String menuCode;

    /** 메뉴 칼로리 */
    private int menuKcal;

    /** 판매상태(0:비판매, 1:판매중)*/
    private boolean menuShow;

    /** 가격 */
    private BigDecimal menuPrice;

    /** 메뉴 영문명 */
    private String menuNameEnglish;

    /** 메뉴 설명 */
    private String menuInformation;

    /** 카테고리 시퀀스(FK) */
    private Long menuCategoryId;

    /** 재료 시퀀스(FK) */
    private Long materialId;

}

