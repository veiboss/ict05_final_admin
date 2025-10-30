package com.boot.ict05_final_admin.domain.analytics.repository;

import com.boot.ict05_final_admin.domain.analytics.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * <p><b>Analytics Repository</b></p>
 *
 * <ul>
 *   <li>카드 요약 및 목록 테이블에 필요한 집계/조회 쿼리의 추상화 인터페이스입니다.</li>
 *   <li>YTD(Year-To-Date) 카드 요약은 <em>항상</em> 올해 1월 1일 ~ 어제 날짜 구간에 대해 집계합니다
 *       (조회 폼의 기간 선택과는 독립).</li>
 *   <li>구현체는 {@code AnalyticsRepositoryImpl} 에서 {@code NamedParameterJdbcTemplate} 또는 QueryDSL 등으로 제공합니다.</li>
 * </ul>
 *
 * @since 1.0
 */
public interface AnalyticsRepository {

    List<StoreOptionDto> findStoreOptions();

    // ===================== Summary Cards =================

    KpiCardsDto findKpiSummary();

    OrdersCardsDto findOrdersSummary();

    List<MaterialsCardsDto> findMaterialsSummary();

    List<TimeCardsDto> findTimeSlicesSummary();

    // ===================== List Rows =====================

    /**
     * KPI 목록(일/월 단위 테이블)에 출력할 집계 행들을 조회합니다.
     *
     * @param cond 조회 조건 DTO (가맹점, 기간, 뷰 단위 등)
     * @return KPI 집계 행 목록
     */
    Page<KpiRowDto> findKpi(AnalyticsSearchDto cond, Pageable pageable);

    /**
     * 주문 분석 목록 테이블 행들을 조회합니다.
     *
     * @param cond 조회 조건 DTO (가맹점, 기간, 뷰 단위 등)
     * @return 주문 집계/상세 행 목록
     */
    Page<OrdersRowDto> findOrders(AnalyticsSearchDto cond, Pageable pageable);

    /**
     * 재료 분석 목록 테이블 행들을 조회합니다.
     *
     * @param cond 조회 조건 DTO (가맹점, 기간, 뷰 단위 등)
     * @return 재료/원가/회전 관련 행 목록
     */
    List<MaterialsRowDto> findMaterials(AnalyticsSearchDto cond);

    /**
     * 시간대/요일 등 타임 슬라이스 분석 목록 행들을 조회합니다.
     *
     * @param cond 조회 조건 DTO (가맹점, 기간, 뷰 단위 등)
     * @return 시간 기반 분석 행 목록
     */
    List<TimeRowDto> findTimeSlices(AnalyticsSearchDto cond);
}