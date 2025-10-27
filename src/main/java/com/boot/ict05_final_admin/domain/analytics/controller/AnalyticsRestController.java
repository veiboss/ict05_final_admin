package com.boot.ict05_final_admin.domain.analytics.controller;

import com.boot.ict05_final_admin.domain.analytics.dto.*;
import com.boot.ict05_final_admin.domain.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 본사 통계/분석 REST 컨트롤러.
 *
 * <p>AJAX 요청을 처리한다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API/analytics")
public class AnalyticsRestController {

    private final AnalyticsService analyticsService;

    /**
     * KPI 집계 조회
     */
    @GetMapping("/kpi")
    public List<KpiRowDto> kpi(AnalyticsSearchDto search) {
        return analyticsService.getKpi(AnalyticsSearchDto.withDefaults(search));
    }

    /**
     * 주문 분석 데이터 조회
     */
    @GetMapping("/orders")
    public List<OrdersRowDto> orders(AnalyticsSearchDto search) {
        return analyticsService.getOrders(AnalyticsSearchDto.withDefaults(search));
    }

    /**
     * 재료 분석 데이터 조회
     */
    @GetMapping("/materials")
    public List<MaterialsRowDto> materials(AnalyticsSearchDto search) {
        return analyticsService.getMaterials(AnalyticsSearchDto.withDefaults(search));
    }

    /**
     * 시간·요일 분석 데이터 조회
     */
    @GetMapping("/time")
    public List<TimeRowDto> time(AnalyticsSearchDto search) {
        return analyticsService.getTimeSlices(AnalyticsSearchDto.withDefaults(search));
    }
}
