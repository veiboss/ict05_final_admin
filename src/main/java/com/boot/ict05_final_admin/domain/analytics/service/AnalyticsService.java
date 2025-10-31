package com.boot.ict05_final_admin.domain.analytics.service;

import com.boot.ict05_final_admin.domain.analytics.dto.*;
import com.boot.ict05_final_admin.domain.analytics.repository.AnalyticsRepository;
import com.boot.ict05_final_admin.domain.analytics.util.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /** KPI 상단 요약 카드 */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public KpiCardsDto selectKpiCards() {
        return analyticsRepository.findKpiSummary();
    }

    /** KPI 목록 */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<KpiRowDto> selectKpis(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findKpi(cond, pageable);
    }

    /** 주문 상단 요약 카드 */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public OrdersCardsDto selectOrdersCards() {
        return analyticsRepository.findOrdersSummary();
    }

    /** 주문 분석 목록 */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<OrdersRowDto> selectOrders(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findOrders(cond, pageable);
    }

    /** 재료 상단 요약 카드 */
    @Transactional(readOnly = true)
    public MaterialsCardsDto selectMaterialsCards(AnalyticsSearchDto cond) {
        return analyticsRepository.findMaterialsSummary(cond); // 단일 DTO로 교체
    }

    /** 재료 분석 목록 */
    @Transactional(readOnly = true)
    public List<MaterialsRowDto> selectMaterials(AnalyticsSearchDto cond) {
        return analyticsRepository.findMaterials(cond);
    }

    /** 시간 상단 요약 카드 */
    @Transactional(readOnly = true)
    public List<TimeCardsDto> selectTimeCards() {
        return analyticsRepository.findTimeSlicesSummary();
    }

    /** 시간·요일 분석 목록 */
    @Transactional(readOnly = true)
    public List<TimeRowDto> selectTimeSlices(AnalyticsSearchDto cond) {
        return analyticsRepository.findTimeSlices(cond);
    }
}
