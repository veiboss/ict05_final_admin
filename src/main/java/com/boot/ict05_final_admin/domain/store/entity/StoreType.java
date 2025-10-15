package com.boot.ict05_final_admin.domain.store.entity;

public enum StoreType {

    /** 직영점 */
    DIRECT("직영점"),

    /** 가맹점 */
    FRANCHISE("가맹점");

    /** 한글 설명 */
    private final String description;

    /**
     * 생성자
     *
     * @param description 각 카테고리의 한글 설명
     */
    StoreType(String description) { this.description = description; }

    /**
     * 카테고리 한글 설명을 반환한다.
     *
     * @return 카테고리 설명
     */
    public String getDescription() { return description; }
}
