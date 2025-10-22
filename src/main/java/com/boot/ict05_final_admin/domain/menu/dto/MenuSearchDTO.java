package com.boot.ict05_final_admin.domain.menu.dto;

import com.boot.ict05_final_admin.domain.menu.entity.MenuCategoryEnum;
import com.boot.ict05_final_admin.domain.menu.entity.MenuShowEnum;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MenuSearchDTO {

    /** 검색 키워드 (상품명, 설명) */
    private String keyword;

    /** 카테고리 필터 (전체 / 세트 / 토스트 / 사이드 / 음료 등) */
    private MenuCategoryEnum menuCategory;

    /** 페이지 번호 (기본값 1) */
    @Builder.Default
    private int page = 1;

    /** 페이지당 표시 개수 (기본값 10) */
    @Builder.Default
    private int size = 10;

    /** 정렬 기준(최신순) */
    @Builder.Default
    private String sort = "menuId";

    /** 정렬 방향 */
    @Builder.Default
    private String direction = "desc";

    /** 검색 조건 헬퍼 */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
}

