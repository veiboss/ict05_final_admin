package com.boot.ict05_final_admin.domain.analytics.service;

import com.boot.ict05_final_admin.domain.analytics.dto.*;
import com.boot.ict05_final_admin.domain.analytics.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * 본사 통계/분석 서비스.
 *
 * <p>QueryDSL 기반 커스텀 리포지토리를 호출해 집계 데이터를 제공한다.</p>
 * <p>카드 요약은 언제나 YTD(올해 1/1 ~ 어제) 고정 구간을 사용한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    // ======================== 상단 요약 카드 =======================

    /** KPI 상단 요약 카드 */
    @Transactional(readOnly = true)
    public List<KpiCardsDto> getKpiCards() {
        return analyticsRepository.findKpiSummary();
    }

    /** 주문 상단 요약 카드 */
    @Transactional(readOnly = true)
    public List<OrdersCardsDto> getOrdersCards() {
        return analyticsRepository.findOrdersSummary();
    }

    /** 재료 상단 요약 카드 */
    @Transactional(readOnly = true)
    public List<MaterialsCardsDto> getMaterialsCards() {
        return analyticsRepository.findMaterialsSummary();
    }

    /** 시간 상단 요약 카드 */
    @Transactional(readOnly = true)
    public List<TimeCardsDto> getTimeCards() {
        return analyticsRepository.findTimeSlicesSummary();
    }

    // ======================== 목록(테이블) ========================

    /** KPI 목록 */
    @Transactional(readOnly = true)
    public List<KpiRowDto> getKpi(AnalyticsSearchDto cond) {
        return analyticsRepository.findKpi(cond);
    }

    /** 주문 분석 목록 */
    @Transactional(readOnly = true)
    public List<OrdersRowDto> getOrders(AnalyticsSearchDto cond) {
        return analyticsRepository.findOrders(cond);
    }

    /** 재료 분석 목록 */
    @Transactional(readOnly = true)
    public List<MaterialsRowDto> getMaterials(AnalyticsSearchDto cond) {
        return analyticsRepository.findMaterials(cond);
    }

    /** 시간·요일 분석 목록 */
    @Transactional(readOnly = true)
    public List<TimeRowDto> getTimeSlices(AnalyticsSearchDto cond) {
        return analyticsRepository.findTimeSlices(cond);
    }
}
