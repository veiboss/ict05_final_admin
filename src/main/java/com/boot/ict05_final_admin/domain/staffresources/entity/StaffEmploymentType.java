package com.boot.ict05_final_admin.domain.staffresources.entity;

/**
 * 직원 근무형태 Enum
 *
 * <p>근무 형태의 종류를 정의하며, 각 항목은 한글 설명(description)을 가진다.</p>
 *
 * <p>주요 근무형태:</p>
 * <ul>
 *     <li>OWNER: 점주</li>
 *     <li>WORKER: 직원</li>
 *     <li>PART_TIMER: 알바</li>
 * </ul>
 */
public enum StaffEmploymentType {

    OWNER("점주"),

    WORKER("직원"),

    PART_TIMER("알바");

    /** 한글 설명 */
    private final String description;

    /**
     * 생성자
     *
     * @param description 각 카테고리의 한글 설명
     */
    StaffEmploymentType(String description) {
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
