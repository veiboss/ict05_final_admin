package com.boot.ict05_final_admin.domain.menu.entity;

public enum MenuCategory {  // enum : 정해진 몇 가지 값만 쓸 수 있는 데이터 타입(코드 안정성+가독성+실수방지)

    TOTAL("전체"),

    SET("세트"),

    TOAST("토스트"),

    SIDE("사이드"),

    DRINK("음료");

    /** 카테고리의 한글 설명 */
    private final String description;

    /** 생성자 */    // 내부적으로는 new MenuCategory("세트") 이런 식으로 동작
    MenuCategory(String description) {this.description = description;}

    /** 카테고리 한글 설명을 반환 */   // description 값을 가져오는 getter
    public String getDescription() { return description; }
}
