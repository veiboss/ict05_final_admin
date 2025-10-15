package com.boot.ict05_final_admin.domain.menu.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long MenuCategoryId;

    /** 상위 카테고리 (대중소 구조 지원) */
    @ManyToOne(fetch = FetchType.LAZY)
    private MenuCategoryEntity menuCategoryParentId;

    /** 카테고리명 */
    @Column(name = "menu_category_name")
    private String menuCategoryName;

    /** 단계 구분(대=1, 중=2, 소=3) */
    @Column(name = "menu_category_level")
    private Short menuCategoryLevel;


}
