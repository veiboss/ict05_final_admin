package com.boot.ict05_final_admin.domain.inventory.entity;

/**
 * 재료 보관방법 Enum
 *
 * <p>재료의 보관방법을 정의하며, 각 항목은 한글 설명(description)을 가진다.</p>
 *
 * <p>주요 카테고리:</p>
 * <ul>
 *     <li>USE: 사용중</li>
 *     <li>STOP : 사용중단</li>
 * </ul>
 */
public enum MaterialStatus {
    /** 상온 */
    USE("상온"),

    /** 냉동 */
    STOP("냉동");

    /** 한글 설명 */
    private final String description;

    /**
     * 생성자
     *
     * @param description 각 카테고리의 한글 설명
     */
    MaterialStatus(String description) {
        this.description = description;
    }

    /**
     * 카테고리 한글 설명을 반환한다.
     *
     * @return 카테고리 설명
     */
    public String getDescription() {
        return description;
    }
}
