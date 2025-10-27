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

    // ======================== 내부 유틸 ========================

    private record Range(
            LocalDate ytdStart, LocalDate ytdEnd,
            LocalDate mtdStart, LocalDate mtdEnd,
            LocalDate pmtStart, LocalDate pmtEnd,
            LocalDate ytdStartPrev, LocalDate ytdEndPrev
    ) {}

    /** 오늘 기준 YTD/MTD/PMTD/전년동기간 범위를 산출한다. */
    private Range ranges() {
        LocalDate today = LocalDate.now();
        LocalDate ytdStart = LocalDate.of(today.getYear(), 1, 1);
        LocalDate ytdEnd   = today.minusDays(1);

        LocalDate mtdStart = today.withDayOfMonth(1);
        LocalDate mtdEnd   = ytdEnd;
        int daysDone = Math.max(0, today.getDayOfMonth() - 1);
        LocalDate pmtStart = mtdStart.minusMonths(1);
        LocalDate pmtEnd   = (daysDone == 0) ? pmtStart.minusDays(1) : pmtStart.plusDays(daysDone - 1);

        LocalDate ytdStartPrev = ytdStart.minusYears(1);
        LocalDate ytdEndPrev   = ytdEnd.minusYears(1);

        return new Range(ytdStart, ytdEnd, mtdStart, mtdEnd, pmtStart, pmtEnd, ytdStartPrev, ytdEndPrev);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private BigDecimal safeDiv(BigDecimal a, BigDecimal b, int scale) {
        a = nz(a);
        b = nz(b);
        if (b.signum() == 0) return BigDecimal.ZERO;
        return a.divide(b, scale, RoundingMode.HALF_UP);
    }

    private List<BigDecimal> fillHourly(Map<Integer, BigDecimal> map) {
        List<BigDecimal> out = new ArrayList<>(Collections.nCopies(24, BigDecimal.ZERO));
        if (map != null) map.forEach((k, v) -> { if (k != null && 0 <= k && k < 24) out.set(k, nz(v)); });
        return out;
    }

    private List<Long> fillHourlyLong(Map<Integer, Long> map) {
        List<Long> out = new ArrayList<>(Collections.nCopies(24, 0L));
        if (map != null) map.forEach((k, v) -> { if (k != null && 0 <= k && k < 24) out.set(k, v == null ? 0L : v); });
        return out;
    }

    private List<BigDecimal> fillDow(Map<Integer, BigDecimal> map) {
        List<BigDecimal> out = new ArrayList<>(Collections.nCopies(7, BigDecimal.ZERO));
        if (map != null) map.forEach((k, v) -> { if (k != null && 0 <= k && k < 7) out.set(k, nz(v)); });
        return out;
    }

    private List<Long> fillDowLong(Map<Integer, Long> map) {
        List<Long> out = new ArrayList<>(Collections.nCopies(7, 0L));
        if (map != null) map.forEach((k, v) -> { if (k != null && 0 <= k && k < 7) out.set(k, v == null ? 0L : v); });
        return out;
    }

    // ======================== KPI 카드 요약 ========================

    /**
     * KPI 카드 요약(YTD 고정).
     * @param storeIds 선택 가맹점 (null 또는 빈 컬렉션이면 전체)
     */
    @Transactional(readOnly = true)
    public KpiCardsDto getKpiCards(Collection<Long> storeIds) {
        var r = ranges();

        BigDecimal ytdSales = analyticsRepository.ytdSales(storeIds, r.ytdStart(), r.ytdEnd());
        Long trx            = analyticsRepository.ytdTransaction(storeIds, r.ytdStart(), r.ytdEnd());
        Long menuQty        = analyticsRepository.ytdMenuQty(storeIds, r.ytdStart(), r.ytdEnd());

        Map<String, BigDecimal> ratio = analyticsRepository.ytdOrderTypeRatioBySales(storeIds, r.ytdStart(), r.ytdEnd());

        BigDecimal mtd     = analyticsRepository.mtdSales(storeIds, r.mtdStart(), r.mtdEnd());
        BigDecimal pmt     = analyticsRepository.pmtSales(storeIds, r.pmtStart(), r.pmtEnd());
        BigDecimal ytdPrev = analyticsRepository.ytdSalesPrevYear(storeIds, r.ytdStartPrev(), r.ytdEndPrev());

        BigDecimal trxBD  = BigDecimal.valueOf(trx == null ? 0L : trx);
        BigDecimal menuBD = BigDecimal.valueOf(menuQty == null ? 0L : menuQty);

        BigDecimal upt = safeDiv(menuBD, trxBD, 2);          // 총판매메뉴/건수
        BigDecimal ads = safeDiv(ytdSales, trxBD, 2);         // 매출/건수
        BigDecimal aur = safeDiv(ytdSales, menuBD, 2);        // 매출/판매메뉴수

        BigDecimal compMoM = (nz(pmt).signum()==0) ? BigDecimal.ZERO
                : nz(mtd).divide(nz(pmt), 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
        BigDecimal compYoY = (nz(ytdPrev).signum()==0) ? BigDecimal.ZERO
                : nz(ytdSales).divide(nz(ytdPrev), 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE);

        return KpiCardsDto.builder()
                .ytdStart(r.ytdStart()).ytdEnd(r.ytdEnd())
                .sales(nz(ytdSales))
                .transaction(trx == null ? 0L : trx)
                .upt(upt).ads(ads).aur(aur)
                .compMoM(compMoM).compYoY(compYoY)
                .visitRatio(nz(ratio.get("visit")))
                .takeoutRatio(nz(ratio.get("takeout")))
                .deliveryRatio(nz(ratio.get("delivery")))
                .build();
    }

    // ======================== 주문 카드 요약 ========================

    /**
     * 주문 카드 요약(YTD 고정).
     */
    @Transactional(readOnly = true)
    public OrdersCardsDto getOrdersCards(Collection<Long> storeIds) {
        var r = ranges();

        BigDecimal ytdSales = analyticsRepository.ytdSales(storeIds, r.ytdStart(), r.ytdEnd());
        Long trx            = analyticsRepository.ytdTransaction(storeIds, r.ytdStart(), r.ytdEnd());
        Long menuCnt        = analyticsRepository.countMenuDistinctYtd(storeIds, r.ytdStart(), r.ytdEnd());
        Long catCnt         = analyticsRepository.countCategoryDistinctYtd(storeIds, r.ytdStart(), r.ytdEnd());
        Map<String, BigDecimal> ratio = analyticsRepository.ytdOrderTypeRatioBySales(storeIds, r.ytdStart(), r.ytdEnd());

        return OrdersCardsDto.builder()
                .ytdStart(r.ytdStart()).ytdEnd(r.ytdEnd())
                .sales(nz(ytdSales))
                .transaction(trx == null ? 0L : trx)
                .menuCount(menuCnt == null ? 0L : menuCnt)
                .categoryCount(catCnt == null ? 0L : catCnt)
                .visitRatio(nz(ratio.get("visit")))
                .takeoutRatio(nz(ratio.get("takeout")))
                .deliveryRatio(nz(ratio.get("delivery")))
                .build();
    }

    // ======================== 재료 카드 요약 ========================

    /**
     * 재료 카드 요약(YTD 고정).
     */
    @Transactional(readOnly = true)
    public MaterialsCardsDto getMaterialsCards(Collection<Long> storeIds) {
        var r = ranges();

        BigDecimal hqNow    = analyticsRepository.hqStockNow();
        BigDecimal storeNow = analyticsRepository.storeStockNow(storeIds);
        BigDecimal orderQty = analyticsRepository.ytdOrderQty(storeIds, r.ytdStart(), r.ytdEnd());
        BigDecimal consQty  = analyticsRepository.ytdConsumptionQty(storeIds, r.ytdStart(), r.ytdEnd());
        BigDecimal sales    = analyticsRepository.ytdSales(storeIds, r.ytdStart(), r.ytdEnd());
        BigDecimal cogs     = analyticsRepository.ytdCogsByRecipeUnitPrice(storeIds, r.ytdStart(), r.ytdEnd());

        BigDecimal profit = nz(sales).subtract(nz(cogs));
        BigDecimal marginRate = safeDiv(profit.multiply(BigDecimal.valueOf(100)), nz(sales), 2);

        BigDecimal avgInv = safeDiv(nz(hqNow).add(nz(storeNow)), BigDecimal.valueOf(2), 3);
        BigDecimal turnover = safeDiv(nz(cogs), avgInv, 2);

        return MaterialsCardsDto.builder()
                .ytdStart(r.ytdStart()).ytdEnd(r.ytdEnd())
                .hqStockQtyNow(nz(hqNow))
                .storeStockQtyNow(nz(storeNow))
                .orderQtyYtd(nz(orderQty))
                .consumptionQtyYtd(nz(consQty))
                .salesAmountYtd(nz(sales))
                .cogsYtd(nz(cogs))
                .profit(profit)
                .marginRate(marginRate)
                .turnover(turnover)
                .build();
    }

    // ======================== 시간·요일 카드 요약 ========================

    /**
     * 시간대/요일 미니차트용 카드 요약(YTD 고정).
     */
    @Transactional(readOnly = true)
    public TimeCardsDto getTimeCards(Collection<Long> storeIds) {
        var r = ranges();

        var salesByHour = analyticsRepository.ytdSalesByHour(storeIds, r.ytdStart(), r.ytdEnd());
        var trxByHour   = analyticsRepository.ytdTrxByHour(storeIds, r.ytdStart(), r.ytdEnd());
        var salesByDow  = analyticsRepository.ytdSalesByDow(storeIds, r.ytdStart(), r.ytdEnd());
        var trxByDow    = analyticsRepository.ytdTrxByDow(storeIds, r.ytdStart(), r.ytdEnd());

        return TimeCardsDto.builder()
                .ytdStart(r.ytdStart()).ytdEnd(r.ytdEnd())
                .salesByHour(fillHourly(salesByHour))
                .trxByHour(fillHourlyLong(trxByHour))
                .salesByDow(fillDow(salesByDow))
                .trxByDow(fillDowLong(trxByDow))
                .build();
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
