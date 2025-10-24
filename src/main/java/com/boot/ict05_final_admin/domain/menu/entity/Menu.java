package com.boot.ict05_final_admin.domain.menu.entity;

import com.boot.ict05_final_admin.domain.menu.dto.MenuModifyFormDTO;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
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

    /** 레시피(연결엔티티) */
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuRecipe> recipe = new ArrayList<>();

    /** menuCategory 참조 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name= "menu_category_id_fk")
    private MenuCategory menuCategory;

    // 수정 편의 메서드
    /** DTO 기반 필드 수정(카테고리는 별도 changeCategory 사용) */
    public void updateMenu(MenuModifyFormDTO dto) {
        if (dto.getMenuName() != null) this.menuName = dto.getMenuName();
        if (dto.getMenuCode() != null) this.menuCode = dto.getMenuCode();
        if (dto.getMenuInformation() != null) this.menuInformation = dto.getMenuInformation();
        if (dto.getMenuNameEnglish() != null) this.menuNameEnglish = dto.getMenuNameEnglish();
        if (dto.getMenuKcal() != null) this.menuKcal = dto.getMenuKcal();
        if (dto.getMenuShow() != null) this.menuShow = dto.getMenuShow(); // Boolean → boolean
        if (dto.getMenuPrice() != null) this.menuPrice = dto.getMenuPrice();
    }

    /** 카테고리 교체 */
    public void changeCategory(MenuCategory category) { this.menuCategory = category; }

}

