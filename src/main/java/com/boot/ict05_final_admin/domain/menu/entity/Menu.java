package com.boot.ict05_final_admin.domain.menu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

    /** 레시피(연결엔티티) */
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuRecipe> recipe = new ArrayList<>();

    /** menuCategory 참조 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name= "menu_category_id_fk")
    private MenuCategory menuCategory;

    /** 메뉴의 알레르기 (재료들의 알레르기 합집합, 파생값) */
    @Transient
    public Set<Allergy> getAllergies() {
        return recipe.stream()
                .map(MenuRecipe::getMaterial)           // 재료
                .filter(Objects::nonNull)
                .flatMap(m -> m.getAllergies().stream()) // 재료의 알레르기 (ManyToMany)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** 연관관계 편의 메서드 */
    public void addRecipe(MenuRecipe item) {
        if (item == null) return;
        recipe.add(item);
        item.setMenu(this);
    }
    public void removeRecipe(MenuRecipe item) {
        if (item == null) return;
        recipe.remove(item);
        item.setMenu(null);
    }
}
