package com.boot.ict05_final_admin.domain.analytics.controller;

import com.boot.ict05_final_admin.domain.analytics.dto.*;
import com.boot.ict05_final_admin.domain.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Page<KpiRowDto>> kpi(AnalyticsSearchDto search, Pageable pageable) {
        Page<KpiRowDto> page = analyticsService.selectKpis(search, pageable);
        System.out.println("레스트컨트롤러 호출");
        return ResponseEntity.ok(page);
    }

    /**
     * 주문 분석 데이터 조회
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrdersRowDto>> orders(AnalyticsSearchDto search) {
        // 기존 시그니처가 List였다면, 우선 ResponseEntity로 래핑만 해둡니다.
        return ResponseEntity.ok(analyticsService.selectOrders(AnalyticsSearchDto.withDefaults(search)));
    }

    /**
     * 재료 분석 데이터 조회
     */
    @GetMapping("/materials")
    public ResponseEntity<List<MaterialsRowDto>> materials(AnalyticsSearchDto search) {
        return ResponseEntity.ok(analyticsService.selectMaterials(AnalyticsSearchDto.withDefaults(search)));
    }

    /**
     * 시간·요일 분석 데이터 조회
     */
    @GetMapping("/time")
    public ResponseEntity<List<TimeRowDto>> time(AnalyticsSearchDto search) {
        return ResponseEntity.ok(analyticsService.selectTimeSlices(AnalyticsSearchDto.withDefaults(search)));
    }
}
