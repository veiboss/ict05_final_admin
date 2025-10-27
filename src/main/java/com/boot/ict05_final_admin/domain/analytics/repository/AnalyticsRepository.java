package com.boot.ict05_final_admin.domain.analytics.repository;

import com.boot.ict05_final_admin.domain.analytics.dto.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    // ===================== 공통 / KPI =====================

    /**
     * YTD 총매출(완료 주문 금액 합계)을 반환합니다.
     *
     * @param storeIds 집계 대상 가맹점 ID 목록(비어있거나 {@code null}이면 전체)
     * @param from     집계 시작일(올해 1/1)
     * @param to       집계 종료일(어제)
     * @return 매출 합계 (없으면 0)
     */
    BigDecimal ytdSales(Collection<Long> storeIds, LocalDate from, LocalDate to);

    /**
     * YTD 결제건수(완료 주문 수)를 반환합니다.
     *
     * @param storeIds 집계 대상 가맹점 ID 목록(비어있거나 {@code null}이면 전체)
     * @param from     집계 시작일(올해 1/1)
     * @param to       집계 종료일(어제)
     * @return 주문 건수 (없으면 0)
     */
    Long ytdTransaction(Collection<Long> storeIds, LocalDate from, LocalDate to);

    /**
     * YTD 총 판매 메뉴 수량(주문 상세의 판매수량 합)을 반환합니다.
     *
     * @param storeIds 집계 대상 가맹점 ID 목록
     * @param from     집계 시작일(올해 1/1)
     * @param to       집계 종료일(어제)
     * @return 판매 수량 합 (없으면 0)
     */
    Long ytdMenuQty(Collection<Long> storeIds, LocalDate from, LocalDate to);

    /**
     * 주문형태별(visit/takeout/delivery) YTD 매출 합계 비율(%)을 반환합니다.
     * <p>반환 맵의 키는 {@code "visit"}, {@code "takeout"}, {@code "delivery"} 입니다.</p>
     *
     * @param storeIds 집계 대상 가맹점 ID 목록
     * @param from     집계 시작일(올해 1/1)
     * @param to       집계 종료일(어제)
     * @return 주문형태별 매출 비율 맵(합계 100, 없으면 0)
     */
    Map<String, BigDecimal> ytdOrderTypeRatioBySales(Collection<Long> storeIds, LocalDate from, LocalDate to);

    /**
     * MTD(이번 달 1일 ~ 어제)의 매출 합계를 반환합니다. (Comp.MoM 계산용)
     *
     * @param storeIds 집계 대상 가맹점 ID 목록
     * @param mFrom    이번 달 시작일(1일)
     * @param mTo      어제
     * @return 매출 합계 (없으면 0)
     */
    BigDecimal mtdSales(Collection<Long> storeIds, LocalDate mFrom, LocalDate mTo);

    /**
     * PMTD(저번 달 같은 일수 구간)의 매출 합계를 반환합니다. (Comp.MoM 계산용)
     *
     * @param storeIds 집계 대상 가맹점 ID 목록
     * @param pFrom    저번 달 시작일(1일)
     * @param pTo      저번 달 {@code mTo}와 동일 일수의 마감일
     * @return 매출 합계 (없으면 0)
     */
    BigDecimal pmtSales(Collection<Long> storeIds, LocalDate pFrom, LocalDate pTo);

    /**
     * 전년 동기간(YTD-1Y)의 매출 합계를 반환합니다. (Comp.YoY 계산용)
     *
     * @param storeIds  집계 대상 가맹점 ID 목록
     * @param fromPrev  전년 YTD 시작일
     * @param toPrev    전년 YTD 종료일
     * @return 매출 합계 (없으면 0)
     */
    BigDecimal ytdSalesPrevYear(Collection<Long> storeIds, LocalDate fromPrev, LocalDate toPrev);

    /**
     * YTD 기준 판매된 <b>메뉴</b>의 DISTINCT 개수를 반환합니다.
     *
     * @param storeIds 집계 대상 가맹점 ID 목록
     * @param from     집계 시작일(올해 1/1)
     * @param to       집계 종료일(어제)
     * @return 메뉴 수(DISTINCT)
     */
    Long countMenuDistinctYtd(Collection<Long> storeIds, LocalDate from, LocalDate to);

    /**
     * YTD 기준 판매된 <b>카테고리</b>의 DISTINCT 개수를 반환합니다.
     *
     * @param storeIds 집계 대상 가맹점 ID 목록
     * @param from     집계 시작일(올해 1/1)
     * @param to       집계 종료일(어제)
     * @return 카테고리 수(DISTINCT)
     */
    Long countCategoryDistinctYtd(Collection<Long> storeIds, LocalDate from, LocalDate to);

    // ===================== Materials =====================

    /**
     * 현재 시점의 본사 재고 수량 합계를 반환합니다.
     *
     * @return 본사 재고 합계 (없으면 0)
     */
    BigDecimal hqStockNow();

    /**
     * 현재 시점의 선택 가맹점 재고 수량 합계를 반환합니다.
     *
     * @param storeIds 대상 가맹점 ID 목록(비어있거나 {@code null}이면 전체)
     * @return 가맹점 재고 합계 (없으면 0)
     */
    BigDecimal storeStockNow(Collection<Long> storeIds);

    /**
     * YTD 자재 발주 수량(가맹점→본사)을 반환합니다.
     *
     * @param storeIds 대상 가맹점 ID 목록
     * @param from     집계 시작일(올해 1/1)
     * @param to       집계 종료일(어제)
     * @return 발주 수량 합 (없으면 0)
     */
    BigDecimal ytdOrderQty(Collection<Long> storeIds, LocalDate from, LocalDate to);

    /**
     * YTD 자재 소진 수량(판매수량×레시피)을 반환합니다.
     *
     * @param storeIds 대상 가맹점 ID 목록
     * @param from     집계 시작일(올해 1/1)
     * @param to       집계 종료일(어제)
     * @return 소진 수량 합 (없으면 0)
     */
    BigDecimal ytdConsumptionQty(Collection<Long> storeIds, LocalDate from, LocalDate to);

    /**
     * YTD 소진 원가(COGS)를 반환합니다.
     * <p>일반적으로 “자재 소비수량 × 유효 단가(기간 내 최신)”의 합으로 계산합니다.</p>
     *
     * @param storeIds 대상 가맹점 ID 목록
     * @param from     집계 시작일(올해 1/1)
     * @param to       집계 종료일(어제)
     * @return COGS 합계 (없으면 0)
     */
    BigDecimal ytdCogsByRecipeUnitPrice(Collection<Long> storeIds, LocalDate from, LocalDate to);

    // ===================== Time Cards (Mini Charts) =====================

    /**
     * 시간대별(0~23) YTD 매출 합계를 반환합니다.
     *
     * @param storeIds 대상 가맹점 ID 목록
     * @param from     집계 시작일(올해 1/1)
     * @param to       집계 종료일(어제)
     * @return {@code hour(0~23) -> 매출(BigDecimal)} 매핑 맵
     */
    Map<Integer, BigDecimal> ytdSalesByHour(Collection<Long> storeIds, LocalDate from, LocalDate to);

    /**
     * 시간대별(0~23) YTD 결제건수 합계를 반환합니다.
     *
     * @param storeIds 대상 가맹점 ID 목록
     * @param from     집계 시작일(올해 1/1)
     * @param to       집계 종료일(어제)
     * @return {@code hour(0~23) -> 건수(Long)} 매핑 맵
     */
    Map<Integer, Long> ytdTrxByHour(Collection<Long> storeIds, LocalDate from, LocalDate to);

    /**
     * 요일별(0=일, 6=토) YTD 매출 합계를 반환합니다.
     *
     * @param storeIds 대상 가맹점 ID 목록
     * @param from     집계 시작일(올해 1/1)
     * @param to       집계 종료일(어제)
     * @return {@code dow(0~6) -> 매출(BigDecimal)} 매핑 맵
     */
    Map<Integer, BigDecimal> ytdSalesByDow(Collection<Long> storeIds, LocalDate from, LocalDate to);

    /**
     * 요일별(0=일, 6=토) YTD 결제건수 합계를 반환합니다.
     *
     * @param storeIds 대상 가맹점 ID 목록
     * @param from     집계 시작일(올해 1/1)
     * @param to       집계 종료일(어제)
     * @return {@code dow(0~6) -> 건수(Long)} 매핑 맵
     */
    Map<Integer, Long> ytdTrxByDow(Collection<Long> storeIds, LocalDate from, LocalDate to);

    // ===================== List Rows =====================

    /**
     * KPI 목록(일/월 단위 테이블)에 출력할 집계 행들을 조회합니다.
     *
     * @param cond 조회 조건 DTO (가맹점, 기간, 뷰 단위 등)
     * @return KPI 집계 행 목록
     */
    List<KpiRowDto> findKpi(AnalyticsSearchDto cond);

    /**
     * 주문 분석 목록 테이블 행들을 조회합니다.
     *
     * @param cond 조회 조건 DTO (가맹점, 기간, 뷰 단위 등)
     * @return 주문 집계/상세 행 목록
     */
    List<OrdersRowDto> findOrders(AnalyticsSearchDto cond);

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