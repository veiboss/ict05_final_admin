package com.boot.ict05_final_admin.domain.menu.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_category_id")
    private Long MenuCategoryId;

    /** 상위 카테고리 (대중소 구조 지원) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "menu_category_parent_id",           // FK 컬럼명
            referencedColumnName = "menu_category_id",  // 참조 PK 컬럼명
            columnDefinition = "BIGINT UNSIGNED"
    )
    private MenuCategoryEntity menuCategoryParentId;

    /** 카테고리명 */
    @Column(name = "menu_category_name")
    private String menuCategoryName;

    /** 단계 구분(대=1, 중=2, 소=3) */
    @Column(name = "menu_category_level")
    private Short menuCategoryLevel;


}
