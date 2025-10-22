package com.boot.ict05_final_admin.domain.menu.entity;

import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuUsageMaterial {

    /** 재료 소진 ID*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_usage_material_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id_fk")
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id_fk")
    private Material material;

    @Column(name = "menu_usage_material_count", nullable = false)
    private Double count;

    @Column(name = "menu_usage_material_unit", length = 20, nullable = false)
    private String unit;
}

