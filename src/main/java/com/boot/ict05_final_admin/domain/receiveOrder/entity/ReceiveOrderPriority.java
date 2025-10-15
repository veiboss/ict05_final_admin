package com.boot.ict05_final_admin.domain.receiveOrder.entity;

public enum ReceiveOrderPriority {

    /** 일반 우선순위 */
    NORMAL("일반"),

    /** 우선 우선순위 */
    URGENT("우선");

    /** 한글 설명 */
    private final String description;

    /**
     * 생성자
     *
     * @param description 각 우선순위의 한글 설명
     */
    ReceiveOrderPriority(String description) { this.description = description; }

    /**
     * 우선순위 한글 설명을 반환한다.
     *
     * @return 우선순위 설명
     */
    public String getDescription() { return description; }
}
