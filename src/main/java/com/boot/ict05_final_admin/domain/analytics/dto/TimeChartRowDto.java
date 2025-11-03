package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TimeChartRowDto {
    private List<String> hours;                 // ["08:00",...,"22:00"]
    private List<String> dows;                  // ["일","월","화","수","목","금","토"]
    private List<ChartSeriesDto> timeOfDay;     // 시간대별 시리즈 (필터적용)
    private List<ChartSeriesDto> dayOfWeek;     // 요일별 시리즈 (필터적용)
}
