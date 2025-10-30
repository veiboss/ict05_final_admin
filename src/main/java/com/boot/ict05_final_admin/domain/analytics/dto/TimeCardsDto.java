package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeCardsDto {
    // 시간대별(0~23) - YTD 누적
    private List<BigDecimal> salesByHour; // 크기 24
    private List<Long>       trxByHour;   // 크기 24

    // 요일별(일~토) - YTD 누적  (0=일, 6=토)
    private List<BigDecimal> salesByDow;  // 크기 7
    private List<Long>       trxByDow;    // 크기 7
}
