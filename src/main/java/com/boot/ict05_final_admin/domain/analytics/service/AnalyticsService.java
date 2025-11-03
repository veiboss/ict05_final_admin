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

import java.util.List;

/**
 * 본사 통계/분석 서비스.
 *
 * <p>QueryDSL 기반 커스텀 리포지토리를 호출해 KPI/주문/재료/시간
 * 관련 집계 데이터를 제공한다. 상단 카드 요약은 YTD(올해 1/1~어제) 고정
 * 구간을 사용하고, 테이블 데이터는 요청 조건 및 페이징에 따른다.</p>
 *
 * <p>조회 계열 메서드는 모두 {@code @Transactional(readOnly = true)}로
 * 설정되어 있어 쓰기 지연/변경 감지 비용을 제거한다.</p>
 *
 * @author
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    /**
     * KPI 상단 요약 카드 조회(YTD).
     *
     * @return KPI 카드 DTO
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public KpiCardsDto selectKpiCards() {
        return analyticsRepository.findKpiSummary();
    }

    /**
     * KPI 테이블 목록 조회.
     *
     * @param cond     검색 조건
     * @param pageable 페이징 정보
     * @return KPI 행 페이지
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<KpiRowDto> selectKpis(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findKpi(cond, pageable);
    }

    /**
     * 주문 상단 요약 카드 조회(YTD).
     *
     * @return 주문 카드 DTO
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public OrdersCardsDto selectOrdersCards() {
        return analyticsRepository.findOrdersSummary();
    }

    /**
     * 주문 분석 테이블 목록 조회.
     *
     * @param cond     검색 조건
     * @param pageable 페이징 정보
     * @return 주문 행 페이지
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<OrdersRowDto> selectOrders(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findOrders(cond, pageable);
    }

    /**
     * 재료 상단 요약 카드 조회(YTD).
     *
     * @return 재료 카드 DTO
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public MaterialsCardsDto selectMaterialsCards() {
        return analyticsRepository.findMaterialsSummary();
    }

    /**
     * 재료 분석 테이블 목록 조회.
     *
     * @param cond     검색 조건
     * @param pageable 페이징 정보
     * @return 재료 행 페이지
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<MaterialsRowDto> selectMaterials(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findMaterials(cond, pageable);
    }

    /** 시간·요일: YTD 누적 차트 */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public TimeChartCardDto selectTimeChartCards() {

        return analyticsRepository.findTimeChartSummary();
    }

    /** 시간·요일: 필터 적용 차트 */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public TimeChartRowDto selectTimeChart(AnalyticsSearchDto cond) {

        return analyticsRepository.findTimeChart(cond);
    }

    /** 시간·요일: 표(상세) */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<TimeRowDto> selectTimeRows(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findTimeRows(cond, pageable);
    }
}
