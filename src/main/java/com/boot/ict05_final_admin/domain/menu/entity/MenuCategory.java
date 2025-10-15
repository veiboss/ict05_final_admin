package com.boot.ict05_final_admin.domain.menu.entity;

public enum MenuCategory {

    TOTAL("전체"),

    SET("세트"),

    TOAST("토스트"),

    SIDE("사이드"),

    DRINK("음료");

    /** 한글 설명 */
    private final String description;

    /** 생성자 */
    MenuCategory(String description) {this.description = description;}

    /** 카테고리 한글 설명을 반환 */
    public String getDescription() { return description; }
}
