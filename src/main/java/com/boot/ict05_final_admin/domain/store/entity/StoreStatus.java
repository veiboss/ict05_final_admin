package com.boot.ict05_final_admin.domain.store.entity;

public enum StoreStatus {

    /** 운영 중인 가맹점 */
    OPERATING("운영"),

    /** 개점 준비 중인 가맹점 */
    PREPARING("개점준비"),

    /** 폐업한 가맹점 */
    CLOSED("폐업");

    /** 한글 설명 */
    private final String description;

    /**
     * 생성자
     *
     * @param description 각 카테고리의 한글 설명
     */
    StoreStatus(String description) { this.description = description; }

    /**
     * 카테고리 한글 설명을 반환한다.
     *
     * @return 카테고리 설명
     */
    public String getDescription() { return description; }
}
