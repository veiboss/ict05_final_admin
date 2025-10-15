package com.boot.ict05_final_admin.domain.menu.entity;

import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

    /** 메뉴 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long menuId;

    /** 메뉴명 */
    @Column(name = "menu_name")
    private String menuName;

    /** 메뉴코드 */
    @Column(name = "menu_code")
    private String menuCode;

    /** 메뉴 설명 */
    private String menuInformation;

    /** 메뉴 영문명 */
    private String menuNameEnglish;

    /** 메뉴 칼로리 */
    @Column(name = "menu_kcal")
    private int menuKcal;

    /** 판매상태(0:비판매, 1:판매중) */
    @Column(name = "menu_show")
    private boolean menuShow;

    /** 가격 */
    @Column(name = "menu_price")
    private BigDecimal menuPrice;

    /** Material 참조 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "material_id_fk")
    private Material material;

    /** menuCategory 참조 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "menu_category_id_fk")
    private MenuCategoryEntity menuCategory;
}
