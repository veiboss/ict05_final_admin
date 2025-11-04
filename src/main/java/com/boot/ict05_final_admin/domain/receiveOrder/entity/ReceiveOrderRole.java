package com.boot.ict05_final_admin.domain.receiveOrder.entity;

/**
 * 수주 역할 구분 Enum
 *
 * <p>같은 테이블을 본사(HQ)와 가맹점(STORE)이 공유하므로
 * 역할(Role)을 명시적으로 구분하기 위한 Enum이다.</p>
 */
public enum ReceiveOrderRole {

    /** 본사 수주 */
    HQ("본사"),

    /** 가맹점 발주 */
    STORE("가맹점");

    /** 한글 설명 */
    private final String description;

    /**
     * 생성자
     *
     * @param description 각 카테고리의 한글 설명
     */
    ReceiveOrderRole(String description) { this.description = description; }

    /**
     * 카테고리 한글 설명을 반환한다.
     *
     * @return 카테고리 설명
     */
    public String getDescription() { return description; }
}
