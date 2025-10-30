package com.boot.ict05_final_admin.domain.analytics.dto;

public enum ViewBy {

    DAY("일별"),
    MONTH("월별");

    /** 한글 설명 */
    private final String description;

    /**
     * 생성자
     *
     * @param description 각 재료의 상태의 한글 설명
     */
    ViewBy(String description) {
        this.description = description;
    }

    /**
     * 재료의 상태 한글 설명을 반환한다.
     *
     * @return 필터 상태 설명
     */
    public String getDescription() {
        return description;
    }
}
