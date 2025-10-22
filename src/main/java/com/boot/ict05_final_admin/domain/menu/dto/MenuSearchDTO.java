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

    /** 검색 대상 (name: 메뉴명, info: 설명, all: 둘 다) */
    @Builder.Default
    private String type = "all";  // 기본값: 메뉴명 + 설명 모두 검색

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

    /** 헬퍼: 검색 타입이 유효한지 검사 */
    public boolean isValidType() {
        return "name".equalsIgnoreCase(type)
                || "info".equalsIgnoreCase(type)
                || "all".equalsIgnoreCase(type);
    }
}

