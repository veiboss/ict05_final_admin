package com.boot.ict05_final_admin.domain.menu.dto;

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

    /** 판매상태(0:비판매, 1:판매중)*/
    private boolean menuShow;

    /** 메뉴명*/
    private String menuName;

    /** 카테고리명 */
    private Long menuCategoryName;

    /** 주재료 */
    private String materialName;

    /** 가격 */
    private BigDecimal menuPrice;

    /** 메뉴 칼로리 */
    private int menuKcal;

}

