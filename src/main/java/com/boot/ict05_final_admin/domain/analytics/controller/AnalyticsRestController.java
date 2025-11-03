// src/main/java/com/boot/ict05_final_admin/domain/analytics/controller/AnalyticsRestController.java
package com.boot.ict05_final_admin.domain.analytics.controller;

import com.boot.ict05_final_admin.domain.analytics.dto.*;
import com.boot.ict05_final_admin.domain.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/API/analytics")
public class AnalyticsRestController {

    private final AnalyticsService analyticsService;

    /** 시간·요일 카드(YTD) */
    @GetMapping("/time/cards")
    public TimeChartCardDto getTimeChartCards() {
        return analyticsService.selectTimeChartCards();
    }

    /** 시간·요일 차트(필터 적용) */
    @GetMapping("/time/chart")
    public TimeChartRowDto getTimeChart(AnalyticsSearchDto cond) {
        return analyticsService.selectTimeChart(AnalyticsSearchDto.withDefaults(cond));
    }

    /** 시간·요일 표 (최대 limit개) */
    @GetMapping("/time")
    public List<TimeRowDto> getTimeRows(AnalyticsSearchDto cond) {
        int limit = (cond.getLimit() == null || cond.getLimit() <= 0) ? 40 : cond.getLimit();
        return analyticsService
                .selectTimeRows(AnalyticsSearchDto.withDefaults(cond), PageRequest.of(0, limit))
                .getContent();  // ✅ List<TimeRowDto>
    }
}
