package com.boot.ict05_final_admin.domain.receiveOrder.entity;

public enum ReceiveOrderStatus {

    /** 접수됨 */
    RECEIVED("접수"),

    /** 배송 준비 중 */
    PREPARING("배송준비"),

    /** 배송 중 */
    SHIPPING("배송중"),

    /** 배송 완료됨 */
    DELIVERED("배송완료");

    /** 한글 설명 */
    private final String description;

    /**
     * 생성자
     *
     * @param description 각 카테고리의 한글 설명
     */
    ReceiveOrderStatus(String description) { this.description = description; }

    /**
     * 카테고리 한글 설명을 반환한다.
     *
     * @return 카테고리 설명
     */
    public String getDescription() { return description; }

}
