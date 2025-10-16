package com.boot.ict05_final_admin.domain.inventory.entity;

/**
 * 재료 카테고리 Enum
 *
 * <p>재료의 카테고리를 정의하며, 각 항목은 한글 설명(description)을 가진다.</p>
 *
 * <p>주요 카테고리:</p>
 * <ul>
 *     <li>BASE: 기본재료</li>
 *     <li>SIDE: 사이드</li>
 *     <li>SAUCE: 소스</li>
 *     <li>TOPPING: 토핑</li>
 *     <li>BEVERAGE: 읍료</li>
 *     <li>PACKAGE: 패키지</li>
 *     <li>ETC: 기타</li>
 * </ul>
 */
public enum MaterialCategory {
    /** 기본재료 */
    BASE("기본재료"),

    /** 사이드 */
    SIDE("사이드"),

    /** 소스 */
    SAUCE("소스"),
    
    /** 토핑 */
    TOPPING("토핑"),
    
    /** 읍료 */
    BEVERAGE("읍료"),
    
    /** 패키지 */
    PACKAGE("패키지"),

    /** 기타 */
    ETC("기타");

    /** 한글 설명 */
    private final String description;

    /**
     * 생성자
     *
     * @param description 각 카테고리의 한글 설명
     */
    MaterialCategory(String description) {
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
