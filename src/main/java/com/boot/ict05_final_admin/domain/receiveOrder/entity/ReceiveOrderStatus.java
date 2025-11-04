package com.boot.ict05_final_admin.domain.receiveOrder.entity;

/**
 * 수주 상태(Enum)
 *
 * <p>본 Enum은 수주의 진행 상태(배송 단계)를 정의한다.<br>
 * 각 상태는 한글 설명과 함께 표시되며, {@link #getDescription()} 메서드를 통해 화면에서 출력된다.</p>
 *
 * <ul>
 *     <li>{@link #RECEIVED} — 주문이 접수된 상태</li>
 *     <li>{@link #PREPARING} — 배송 준비 중 (출고 대기)</li>
 *     <li>{@link #SHIPPING} — 배송 중 (물류 이동 단계)</li>
 *     <li>{@link #DELIVERED} — 배송 완료 (가맹점 수령 완료)</li>
 * </ul>
 *
 * <p>주요 사용처:</p>
 * <ul>
 *     <li>수주 현황 목록 및 상세 화면의 상태 배지</li>
 *     <li>배송 상태 자동 갱신 로직 ({@code ReceiveOrderService.advanceStatus()})</li>
 * </ul>
 *
 * @author 최민진
 * @since 2025.10
 */
public enum ReceiveOrderStatus {

    /** 접수됨 */
    RECEIVED("접수"),

    /** 배송 중 */
    SHIPPING("배송"),

    /** 배송 완료됨 */
    DELIVERED("완료");

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
