package com.boot.ict05_final_admin.domain.menu.entity;

import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_recipe_id")
    private Long menuRecipeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id_fk", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id_fk", nullable = false)
    private Material material;

    @Column(name = "recipe_qty", precision = 12, scale = 3, nullable = false)
    private BigDecimal recipeQty;

    @Column(name = "recipe_unit", length = 20, nullable = false)
    private String recipeUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipe_role", length = 10, nullable = false)
    private RecipeRole recipeRole; // MAIN / SAUCE / TOPPING

    @Column(name = "recipe_sort", nullable = false)
    private Integer recipeSort;

    public enum RecipeRole { MAIN, SAUCE  }
}
