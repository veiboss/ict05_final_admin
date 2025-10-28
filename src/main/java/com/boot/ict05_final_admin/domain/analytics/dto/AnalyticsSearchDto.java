package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * 통계/분석 공통 검색 DTO.
 *
 * <p>가맹점 선택, 기간, 보기방식(일/월), 출력개수 등을 포함한다.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsSearchDto {

    /** 가맹점 ID 목록(미지정 시 전체) */
    private List<Long> storeIds;

    /** 조회 시작일 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 조회 종료일 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 보기 방식: DAY / MONTH */
    private ViewBy viewBy = ViewBy.DAY;

    /** 출력 개수(테이블 상단 셀렉트와 연동) */
    private Integer limit = 50;

    public enum ViewBy { DAY, MONTH }

    /**
     * 컨트롤러/REST에서 NPE 방지를 위해 기본값을 주입한다.
     */
    public static AnalyticsSearchDto withDefaults(AnalyticsSearchDto in) {
        AnalyticsSearchDto s = (in == null) ? new AnalyticsSearchDto() : in;
        LocalDate today = LocalDate.now();

        if (s.getStartDate() == null) s.setStartDate(today.withDayOfYear(1));
        if (s.getEndDate() == null)   s.setEndDate(today);
        if (s.getViewBy() == null)    s.setViewBy(ViewBy.DAY);
        if (s.getLimit() == null)     s.setLimit(40);

        return s;
    }
}
