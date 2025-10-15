package com.boot.ict05_final_admin.domain.store.entity;

/** 매장 상태 ENUM
 *
 * <p>가맹점의 상태의 분류를 정의하며, </p>
 *
 * <p>주요 카테고리:</p>
 * <ul>
 *     <li>OPEN: 운영</li>
 *     <li>PREPARING: 개점준비</li>
 *     <li>CLOSED: 폐업</li>
 * </ul>
 *
 */
public enum StoreStatus {
    OPEN("운영"),
    PREPARING("개점준비"),
    CLOSED("폐업");

    /** 한글 설명 */
    private final String label;

    /**
     * 생성자. 상수 선언부의 괄호 값이 여기로 들어와서 label에 저장됨.
     *  예: OPEN("운영") → 생성자 호출 → label에 "운영" 저장.
     */
    StoreStatus(String label) { this.label = label; }

    /**
     * 카테고리 한글 설명을 반환한다.
     *
     * @return 카테고리 설명
     */
    public String getLabel() { return label; }
}
