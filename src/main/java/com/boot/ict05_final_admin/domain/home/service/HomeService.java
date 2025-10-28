package com.boot.ict05_final_admin.domain.home.service;

import com.boot.ict05_final_admin.domain.home.dto.*;
import com.boot.ict05_final_admin.domain.home.repository.HomeRepositoryCustom;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final HomeRepositoryCustom homeRepository;

    /**
     * 대시보드 뷰 데이터 조립
     * - 기간 기준:
     *   * KPI: 이번 달 시작 ~ 다음 달 시작
     *   * 월간 차트: 최근 6개월(이번 달 포함) 시작 ~ 다음 달 시작
     *   * 주간 차트: 이번 주 월요일 00:00 ~ 일요일 24:00 (Repository는 주간 집계 메서드 사용)
     *   * 물류 매출: 월간 차트와 동일한 범위
     */
    public DashboardViewDTO buildDashboard() {

        LocalDateTime now = LocalDateTime.now();

        // 이번 달
        LocalDateTime monthStart = YearMonth.from(now).atDay(1).atStartOfDay();
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);

        // 최근 6개월(이번 달 포함) → monthStart 기준으로 5개월 전부터
        LocalDateTime sixMonthsStart = monthStart.minusMonths(5);

        // 이번 주(월 ~ 일) 시작
        LocalDate weekMonday = now.toLocalDate().with(DayOfWeek.MONDAY);
        LocalDateTime weekStart = weekMonday.atStartOfDay();

        // 필터(매장 선택 없으면 null)
        @Nullable Set<Long> storeFilter = null;

        // 1) KPI
        KpiSummary kpi = homeRepository.kpiSummary(monthStart, nextMonthStart, storeFilter);

        long kpiRevenueThisMonth   = kpi != null ? kpi.revenueThisMonth()   : 0L;
        int  kpiActiveStores       = kpi != null ? kpi.activeStores()       : 0;
        long kpiOrderCount         = kpi != null ? kpi.orderCount()         : 0L;
        int  kpiNewStores          = kpi != null ? kpi.newStores()          : 0;
        double kpiRevenueGrowthPct = kpi != null ? kpi.revenueGrowthPct()   : 0.0;

        // 2) 월별 매출(최근 6개월)
        List<Point<Long>> salesMonthPoints =
                homeRepository.salesByMonth(sixMonthsStart, nextMonthStart, storeFilter);

        // 라벨은 "n월"로, 값은 long
        List<String> salesLabels = salesMonthPoints.stream()
                .map(p -> p.at().getMonthValue() + "월")
                .toList();
        List<Long> salesValues = salesMonthPoints.stream()
                .map(Point::value)
                .map(v -> v == null ? 0L : v)
                .toList();

        // 3) 주간 매출(이번 주)
        List<Point<Long>> weeklyPoints =
                homeRepository.salesByWeek(weekStart, storeFilter);

        // 라벨: 월~일
        final String[] WEEK_KO = { "월","화","수","목","금","토","일" };
        List<String> weeklyLabels = new ArrayList<>();
        List<Long> weeklyValues = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weeklyLabels.add(WEEK_KO[i]);
            long val = 0L;
            if (i < weeklyPoints.size() && weeklyPoints.get(i) != null && weeklyPoints.get(i).value() != null) {
                val = weeklyPoints.get(i).value();
            }
            weeklyValues.add(val);
        }

        // 4) 물류 매출(월간, 최근 6개월)
        List<Point<Long>> logiMonthPoints =
                homeRepository.logisticsByMonth(sixMonthsStart, nextMonthStart, storeFilter);
        List<String> logiLabels = logiMonthPoints.stream()
                .map(p -> p.at().getMonthValue() + "월")
                .toList();
        List<Long> logiValues = logiMonthPoints.stream()
                .map(Point::value)
                .map(v -> v == null ? 0L : v)
                .toList();

        // 5) 매장별 매출 랭킹(이번 달) + 증감률(전월 대비)
        int topLimit = 10;

        // 이번 달 상위 매장
        List<StoreRevenue> topStores =
                homeRepository.topStores(monthStart, nextMonthStart, storeFilter, topLimit);

        // 전월 기간
        LocalDateTime prevMonthStart = monthStart.minusMonths(1);
        LocalDateTime prevMonthEnd   = monthStart;

        // 전월 대비 증감률
        List<StoreGrowth> growthRows =
                homeRepository.storeGrowth(monthStart, nextMonthStart,
                        prevMonthStart, prevMonthEnd,
                        storeFilter, topLimit);

        // growth를 id→pct 로 매핑
        Map<Long, Double> growthPctById = growthRows.stream()
                .collect(Collectors.toMap(StoreGrowth::storeId, StoreGrowth::growthPct, (a, b) -> a));

        // 화면 테이블 DTO로 변환
        List<DashboardViewDTO.StoreRow> storeRows = topStores.stream()
                .map(r -> new DashboardViewDTO.StoreRow(
                        r.storeName(),                                 // 매장명
                        r.revenue(),                               // 매출
                        growthPctById.getOrDefault(r.storeId(), 0.0) // 증감률(%)
                ))
                .toList();

        // 최종 조립
        return new DashboardViewDTO(
                // KPI
                kpiRevenueThisMonth,
                kpiActiveStores,
                kpiOrderCount,
                kpiNewStores,
                kpiRevenueGrowthPct,

                // 월별 매출
                salesLabels, salesValues,

                // 주간 매출
                weeklyLabels, weeklyValues,

                // 물류 매출
                logiLabels, logiValues,

                // 표
                storeRows
        );
    }

}
