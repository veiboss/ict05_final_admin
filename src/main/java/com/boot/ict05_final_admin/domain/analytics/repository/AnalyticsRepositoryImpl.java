package com.boot.ict05_final_admin.domain.analytics.repository;

import com.boot.ict05_final_admin.domain.analytics.dto.*;
import com.boot.ict05_final_admin.domain.order.entity.OrderStatus;
import com.boot.ict05_final_admin.domain.order.entity.OrderType;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.boot.ict05_final_admin.domain.order.entity.QCustomerOrder;
import com.boot.ict05_final_admin.domain.order.entity.QCustomerOrderDetail;
import com.boot.ict05_final_admin.domain.store.entity.QStore;
import com.boot.ict05_final_admin.domain.menu.entity.QMenu;
import com.boot.ict05_final_admin.domain.menu.entity.QMenuCategory;
import com.boot.ict05_final_admin.domain.inventory.entity.QStoreInventory;
import com.boot.ict05_final_admin.domain.inventory.entity.QStoreMaterial;
import com.boot.ict05_final_admin.domain.inventory.entity.QMaterial;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.QReceiveOrder;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.QReceiveOrderDetail;

@Repository
@RequiredArgsConstructor
public class AnalyticsRepositoryImpl implements AnalyticsRepository {

    private final JPAQueryFactory query;

    private final QCustomerOrder co = QCustomerOrder.customerOrder;
    private final QCustomerOrderDetail cod = QCustomerOrderDetail.customerOrderDetail;
    private final QStore s = QStore.store;
    private final QMenu m = QMenu.menu;
    private final QMenuCategory mc = QMenuCategory.menuCategory;
    private final QStoreInventory si = QStoreInventory.storeInventory;
    private final QStoreMaterial sm = QStoreMaterial.storeMaterial;
    private final QMaterial mat = QMaterial.material;
    private final QReceiveOrder ro = QReceiveOrder.receiveOrder;
    private final QReceiveOrderDetail rod = QReceiveOrderDetail.receiveOrderDetail;

    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");
    private static record SidLabelKey(Long sid, String label) {}
    private static class GlobalComp {
        final BigDecimal compMoM;
        final BigDecimal compYoY;
        GlobalComp(BigDecimal mom, BigDecimal yoy) {
            this.compMoM = mom; this.compYoY = yoy;
        }
    }


    @Override
    public List<StoreOptionDto> findStoreOptions() {
        // s.name 또는 s.storeName 등 실제 컬럼명에 맞게 수정
        return query
                .select(Projections.constructor(
                        StoreOptionDto.class,
                        s.id,
                        s.name
                ))
                .from(s)
                .orderBy(s.name.asc())
                .fetch();
    }


    // ====== 공통 유틸 ======

    /** analyticsSearchDto.getStoreIds() 가 비었으면 null(무시), 있으면 IN 절 */
    private BooleanExpression inStores(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) return null;
        // s.storeId 라는 PK 경로를 사용한다고 가정 (필요 시 컬럼명 맞추기)
        return s.id.in(storeIds);
    }

    /** 날짜 폐구간-개구간: [start 00:00:00, end+1 00:00:00) — 인덱스 사용 가능 */
    private BooleanExpression betweenDateClosedOpen(DateTimePath<LocalDateTime> path,
                                                    LocalDate start, LocalDate end) {
        if (start == null && end == null) return null;
        LocalDateTime from = (start != null ? start.atStartOfDay() : LocalDate.MIN.atStartOfDay());
        LocalDateTime toEx = (end   != null ? end.plusDays(1).atStartOfDay() : LocalDate.MAX.atStartOfDay());
        return path.goe(from).and(path.lt(toEx));
    }

    /** 올해 1월1일 ~ 어제 날짜(아시아/서울) */
    private LocalDate[] thisYearToYesterday() {
        LocalDate today = LocalDate.now(ZONE_SEOUL);
        return new LocalDate[] {
                LocalDate.of(today.getYear(), 1, 1),
                today.minusDays(1)
        };
    }

    private static final NumberExpression<BigDecimal> BD_100 =
            Expressions.numberTemplate(BigDecimal.class, "cast(100 as big_decimal)");


    // BigDecimal 캐스팅은 'big_decimal' 만 허용
    private NumberExpression<BigDecimal> toBigDecimal(NumberExpression<? extends Number> exp) {
        return Expressions.numberTemplate(BigDecimal.class, "cast({0} as big_decimal)", exp);
    }

    /** 숫자 나눗셈(0 분모 보호) -> DECIMAL(38,6) 정도로 강제하여 정밀도 확보 */
    private NumberExpression<BigDecimal> safeDiv(NumberExpression<BigDecimal> numerator,
                                                 NumberExpression<BigDecimal> denominator) {
        return Expressions.numberTemplate(
                BigDecimal.class,
                "CASE WHEN {0} = 0 THEN cast(0 as big_decimal) ELSE ({1} / {0}) END",
                denominator, numerator
        );
    }

    /** SUM(IF(조건, 값, 0)) */
    private NumberExpression<BigDecimal> sumIf(BooleanExpression condition, NumberExpression<BigDecimal> value) {
        return Expressions.numberTemplate(
                BigDecimal.class,
                "SUM(CASE WHEN {0} THEN {1} ELSE cast(0 as big_decimal) END)",
                condition, value
        );
    }

    /** COUNT(IF(조건, 1, NULL)) = 조건 건수 */
    private NumberExpression<Long> countIf(BooleanExpression condition) {
        return Expressions.numberTemplate(Long.class, "SUM(CASE WHEN {0} THEN 1 ELSE 0 END)", condition);
    }

    /** MariaDB DATE_FORMAT(date, fmt) */
    private StringExpression dateFormat(Expression<?> dateTime, String fmt) {
        return Expressions.stringTemplate("DATE_FORMAT({0}, {1})", dateTime, Expressions.constant(fmt));
    }

    /** BigDecimal SUM coalesce(0) */
    private NumberExpression<BigDecimal> sumBd(NumberExpression<BigDecimal> exp) {
        return exp.coalesce(BigDecimal.ZERO);
    }

    /** Long COUNT coalesce(0) */
    private NumberExpression<Long> countLong(NumberExpression<Long> exp) {
        return exp.coalesce(0L);
    }

    /** 0-나눗셈 보호 + 반올림 */
    private static BigDecimal divOrZero(BigDecimal num, BigDecimal den, int scale) {
        return (den == null || den.signum() == 0)
                ? BigDecimal.ZERO
                : num.divide(den, scale, RoundingMode.HALF_UP);
    }

    // 보조: % 값 스케일 정리 (선택)
    private static BigDecimal ytdScale(BigDecimal v, int scale) {
        return v.setScale(scale, RoundingMode.HALF_UP);
    }

    // ====== 1) KPI 요약카드 : 올해 1/1 ~ 어제 누적 ======
    @Override
    public KpiCardsDto findKpiSummary() {

        final BooleanExpression done = co.status.eq(OrderStatus.COMPLETED);

        // 아시아/서울 기준 기간 경계 계산
        final var today     = LocalDate.now(ZONE_SEOUL);
        final var ytdStart  = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd    = today.minusDays(1);

        final var mtdStart  = today.withDayOfMonth(1);
        final var mtdEnd    = ytdEnd;

        final long dayCount = java.time.temporal.ChronoUnit.DAYS.between(mtdStart, mtdEnd) + 1;
        final var pmtStart  = mtdStart.minusMonths(1);
        var       pmtEnd    = pmtStart.plusDays(dayCount - 1);
        final var pmtLast   = pmtStart.plusMonths(1).minusDays(1);
        if (pmtEnd.isAfter(pmtLast)) pmtEnd = pmtLast;

        final var lytdStart = ytdStart.minusYears(1);
        final var lytdEnd   = ytdEnd.minusYears(1);

        // --- 집계 쿼리들 (전부 파라미터 바운딩)
        BigDecimal ytdSales = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co)
                        .where(done, betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        long ytdTrx = Optional.ofNullable(
                query.select(co.id.count())
                        .from(co)
                        .where(done, betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd))
                        .fetchOne()
        ).orElse(0L);

        BigDecimal mtdSales = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co)
                        .where(done, betweenDateClosedOpen(co.orderedAt, mtdStart, mtdEnd))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        BigDecimal pmtSales = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co)
                        .where(done, betweenDateClosedOpen(co.orderedAt, pmtStart, pmtEnd))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        BigDecimal lytdSales = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co)
                        .where(done, betweenDateClosedOpen(co.orderedAt, lytdStart, lytdEnd))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        // YTD 판매수량
        Integer ytdUnitsI = Optional.ofNullable(
                query.select(cod.quantity.sum())
                        .from(cod).join(cod.order, co)
                        .where(done, betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd))
                        .fetchOne()
        ).orElse(0);
        BigDecimal ytdUnits = BigDecimal.valueOf(ytdUnitsI.longValue());

        // 채널 비율(YTD)
        BigDecimal ytdVisit = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co)
                        .where(done, co.orderType.eq(OrderType.VISIT),
                                betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        BigDecimal ytdTakeout = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co)
                        .where(done, co.orderType.eq(OrderType.TAKEOUT),
                                betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        BigDecimal ytdDelivery = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co)
                        .where(done, co.orderType.eq(OrderType.DELIVERY),
                                betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        // 파생 KPI 계산
        BigDecimal trxBd = (ytdTrx <= 0) ? BigDecimal.ZERO : BigDecimal.valueOf(ytdTrx);
        BigDecimal ads   = divOrZero(ytdSales, trxBd, 2); // 원/건
        BigDecimal upt   = divOrZero(ytdUnits, trxBd, 6); // 개/건
        BigDecimal aur   = divOrZero(ytdSales, ytdUnits, 2); // 원/개

        BigDecimal compMoM = (pmtSales.signum()==0) ? BigDecimal.ZERO
                : ytdScale(mtdSales.divide(pmtSales, 6, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100)), 1);

        BigDecimal compYoY = (lytdSales.signum()==0) ? BigDecimal.ZERO
                : ytdScale(ytdSales.divide(lytdSales, 6, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100)), 1);

        BigDecimal visitR = (ytdSales.signum()==0) ? BigDecimal.ZERO
                : ytdVisit.multiply(BigDecimal.valueOf(100))
                .divide(ytdSales, 1, RoundingMode.HALF_UP);

        BigDecimal takeR  = (ytdSales.signum()==0) ? BigDecimal.ZERO
                : ytdTakeout.multiply(BigDecimal.valueOf(100))
                .divide(ytdSales, 1, RoundingMode.HALF_UP);

        BigDecimal delivR = (ytdSales.signum()==0) ? BigDecimal.ZERO
                : ytdDelivery.multiply(BigDecimal.valueOf(100))
                .divide(ytdSales, 1, RoundingMode.HALF_UP);

        return KpiCardsDto.builder()
                .sales(ytdSales)
                .transaction(ytdTrx)
                .upt(upt)
                .ads(ads)
                .aur(aur)
                .compMoM(compMoM)
                .compYoY(compYoY)
                .visitRatio(visitR)
                .takeoutRatio(takeR)
                .deliveryRatio(delivR)
                .build();
    }


    // KPI 목록(일/월 단위 집계) 조회 - kpi.html 테이블 데이터
    @Override
    public Page<KpiRowDto> findKpi(AnalyticsSearchDto analyticsSearchDto, Pageable pageable) {
        boolean byMonth = analyticsSearchDto.getViewBy() == ViewBy.MONTH;
        String fmt = byMonth ? "%Y-%m" : "%Y-%m-%d";
        StringExpression labelExpr = dateFormat(co.orderedAt, fmt);

        BooleanExpression baseFilter = eqKpiFilter(analyticsSearchDto, co, s);

        Long total = Optional.ofNullable(
                query.select(Expressions.numberTemplate(Long.class, "COUNT(*)"))
                        .from(co).join(co.storeIdFk, s)
                        .where(baseFilter)
                        .groupBy(s.id, labelExpr)
                        .fetch()
        ).map(List::size).map(Integer::longValue).orElse(0L);

        if (total == 0L) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0L);
        }

        List<Tuple> baseRows = query
                .select(
                        s.id, s.name, labelExpr,
                        co.totalPrice.sum(),
                        co.id.count(),
                        cod.quantity.sum()
                )
                .from(co)
                .join(co.storeIdFk, s)
                .leftJoin(cod).on(cod.order.eq(co))
                .where(baseFilter)
                .groupBy(s.id, s.name, labelExpr)
                .orderBy(labelExpr.desc(), co.totalPrice.sum().coalesce(BigDecimal.ZERO).desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (baseRows.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }

        // 페이지에 보이는 점포 id 집합
        Set<Long> pageSids = new HashSet<>();
        for (Tuple t : baseRows) pageSids.add(t.get(0, Long.class));

        List<Long> selected = analyticsSearchDto.getStoreIds();
        boolean isAll = (selected == null || selected.isEmpty());
        boolean isMulti = (selected != null && selected.size() > 1);

        Map<Long, GlobalComp> perStoreComp = null;
        GlobalComp globalComp = null;

        if (isAll || isMulti) {
            // 여러 점포 선택 시에도 매장별 Comp를 계산
            Set<Long> targetSids = isAll ? pageSids : new HashSet<>(selected);
            perStoreComp = computeCompByStore(targetSids);
        } else {
            // 단일 점포 선택 시
            globalComp = computeGlobalComp(analyticsSearchDto);
        }

        List<KpiRowDto> content = new ArrayList<>(baseRows.size());
        for (Tuple t : baseRows) {
            Long sid        = t.get(0, Long.class);
            String storeNm  = t.get(1, String.class);
            String label    = t.get(2, String.class);
            BigDecimal sales= Optional.ofNullable(t.get(3, BigDecimal.class)).orElse(BigDecimal.ZERO);
            Long trx        = Optional.ofNullable(t.get(4, Long.class)).orElse(0L);
            Integer unitsI  = Optional.ofNullable(t.get(5, Integer.class)).orElse(0);

            BigDecimal units = BigDecimal.valueOf(unitsI.longValue());
            BigDecimal trxBd = trx <= 0 ? BigDecimal.ZERO : BigDecimal.valueOf(trx);

            BigDecimal ads = divOrZero(sales, trxBd, 2);
            BigDecimal upt = divOrZero(units, trxBd, 6);
            BigDecimal aur = divOrZero(sales, units, 2);

            GlobalComp compVal = (isAll || isMulti)
                    ? perStoreComp.getOrDefault(sid, new GlobalComp(BigDecimal.ZERO, BigDecimal.ZERO))
                    : globalComp;

            content.add(KpiRowDto.builder()
                    .date(label)
                    .storeName(storeNm)
                    .sales(sales)
                    .transaction(trx)
                    .upt(upt)
                    .ads(ads)
                    .aur(aur)
                    .compMoM(compVal.compMoM)
                    .compYoY(compVal.compYoY)
                    .build());
        }

        return new PageImpl<>(content, pageable, total);
    }

    /* ===================== Helper Methods ===================== */

    /** 필터 빌더 */
    private BooleanExpression eqKpiFilter(AnalyticsSearchDto analyticsSearchDto, QCustomerOrder co, QStore s) {
        BooleanExpression where = Expressions.asBoolean(true).isTrue();

        // 상태: 완료
        where = where.and(co.status.eq(OrderStatus.COMPLETED));

        // 기간(폐구간: [start, end))
        if (analyticsSearchDto.getStartDate() != null && analyticsSearchDto.getEndDate() != null) {
            where = where.and(betweenDateClosedOpen(co.orderedAt, analyticsSearchDto.getStartDate(), analyticsSearchDto.getEndDate()));
        }

        // 점포 필터 (전체/all 은 무시)
        List<Long> storeIds = analyticsSearchDto.getStoreIds();
        if (storeIds != null && !storeIds.isEmpty()) {
            where = where.and(s.id.in(storeIds));
        }
        return where;
    }

    /** DATE_FORMAT(co.orderedAt, '%Y-%m' | '%Y-%m-%d') */
    private StringExpression dateFormat(DateTimePath<LocalDateTime> dt, String pattern) {
        return Expressions.stringTemplate(
                "DATE_FORMAT({0}, {1})", dt, Expressions.constant(pattern)
        );
    }

    /** 선택된 매장 집합(없으면 전사)에 대해 MTD/PMTD, YTD/LYTD를 단일 집계쿼리로 계산 */
    private GlobalComp computeGlobalComp(AnalyticsSearchDto cond) {
        BooleanExpression base = co.status.eq(OrderStatus.COMPLETED);
        if (cond.getStoreIds() != null && !cond.getStoreIds().isEmpty()) {
            base = base.and(s.id.in(cond.getStoreIds()));   // 선택 매장만
        }

        final var today     = LocalDate.now(ZONE_SEOUL);
        final var mtdStart  = today.withDayOfMonth(1);
        final var mtdEnd    = today.minusDays(1);
        final long dayCount = java.time.temporal.ChronoUnit.DAYS.between(mtdStart, mtdEnd) + 1;

        final var pmtStart  = mtdStart.minusMonths(1);
        var       pmtEnd    = pmtStart.plusDays(dayCount - 1);
        final var pmtLast   = pmtStart.plusMonths(1).minusDays(1);
        if (pmtEnd.isAfter(pmtLast)) pmtEnd = pmtLast;

        final var ytdStart  = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd    = today.minusDays(1);
        final var lytdStart = ytdStart.minusYears(1);
        final var lytdEnd   = ytdEnd.minusYears(1);

        BigDecimal curMtd = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co).join(co.storeIdFk, s)
                        .where(base, betweenDateClosedOpen(co.orderedAt, mtdStart, mtdEnd))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        BigDecimal prevMtd = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co).join(co.storeIdFk, s)
                        .where(base, betweenDateClosedOpen(co.orderedAt, pmtStart, pmtEnd))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        BigDecimal curYtd = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co).join(co.storeIdFk, s)
                        .where(base, betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        BigDecimal lastYtd = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co).join(co.storeIdFk, s)
                        .where(base, betweenDateClosedOpen(co.orderedAt, lytdStart, lytdEnd))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        BigDecimal compMoM = (prevMtd.signum()==0) ? BigDecimal.ZERO
                : curMtd.divide(prevMtd, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));

        BigDecimal compYoY = (lastYtd.signum()==0) ? BigDecimal.ZERO
                : curYtd.divide(lastYtd, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));

        return new GlobalComp(compMoM, compYoY);
    }

    /** 전체 선택일 때 페이지에 노출된 점포들에 대해 점포별 Comp 계산 (MTD/PMTD, YTD/LYTD) */
    private Map<Long, GlobalComp> computeCompByStore(Set<Long> sidsOnPage) {
        Map<Long, GlobalComp> out = new HashMap<>();
        if (sidsOnPage == null || sidsOnPage.isEmpty()) return out;

        final var today     = LocalDate.now(ZONE_SEOUL);
        final var mtdStart  = today.withDayOfMonth(1);
        final var mtdEnd    = today.minusDays(1);
        final long dayCount = java.time.temporal.ChronoUnit.DAYS.between(mtdStart, mtdEnd) + 1;

        final var pmtStart  = mtdStart.minusMonths(1);
        var       pmtEnd    = pmtStart.plusDays(dayCount - 1);
        final var pmtLast   = pmtStart.plusMonths(1).minusDays(1);
        if (pmtEnd.isAfter(pmtLast)) pmtEnd = pmtLast;

        final var ytdStart  = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd    = today.minusDays(1);
        final var lytdStart = ytdStart.minusYears(1);
        final var lytdEnd   = ytdEnd.minusYears(1);

        List<Tuple> rows = query
                .select(
                        s.id,
                        sumIf(betweenDateClosedOpen(co.orderedAt, mtdStart, mtdEnd), co.totalPrice),
                        sumIf(betweenDateClosedOpen(co.orderedAt, pmtStart, pmtEnd), co.totalPrice),
                        sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd), co.totalPrice),
                        sumIf(betweenDateClosedOpen(co.orderedAt, lytdStart, lytdEnd), co.totalPrice)
                )
                .from(co)
                .join(co.storeIdFk, s)
                .where(co.status.eq(OrderStatus.COMPLETED), s.id.in(sidsOnPage))
                .groupBy(s.id)
                .fetch();

        for (Tuple t : rows) {
            Long sid       = t.get(0, Long.class);
            BigDecimal mtd = Optional.ofNullable(t.get(1, BigDecimal.class)).orElse(BigDecimal.ZERO);
            BigDecimal pmt = Optional.ofNullable(t.get(2, BigDecimal.class)).orElse(BigDecimal.ZERO);
            BigDecimal ytd = Optional.ofNullable(t.get(3, BigDecimal.class)).orElse(BigDecimal.ZERO);
            BigDecimal ly  = Optional.ofNullable(t.get(4, BigDecimal.class)).orElse(BigDecimal.ZERO);

            BigDecimal mom = (pmt.signum()==0) ? BigDecimal.ZERO
                    : mtd.divide(pmt, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
            BigDecimal yoy = (ly.signum()==0) ? BigDecimal.ZERO
                    : ytd.divide(ly, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));

            out.put(sid, new GlobalComp(mom, yoy));
        }
        return out;
    }

    /** 비교기간(라벨 세트) 매출 맵 조회: (점포×라벨) → 매출 */
    private Map<SidLabelKey, BigDecimal> fetchCompareSalesMap(AnalyticsSearchDto analyticsSearchDto,
                                                              Set<Long> sids,
                                                              Set<String> labelSet,
                                                              String fmt) {
        Map<SidLabelKey, BigDecimal> out = new HashMap<>();
        if (labelSet == null || labelSet.isEmpty() || sids == null || sids.isEmpty()) return out;

        StringExpression le = dateFormat(co.orderedAt, fmt);
        BooleanExpression filter = eqKpiFilter(analyticsSearchDto, co, s)
                .and(s.id.in(sids))
                .and(le.in(labelSet));

        List<Tuple> rows = query
                .select(s.id, le, co.totalPrice.sum())
                .from(co)
                .join(co.storeIdFk, s)
                .where(filter)
                .groupBy(s.id, le)
                .fetch();

        for (Tuple t : rows) {
            Long sid = t.get(0, Long.class);
            String lbl = t.get(1, String.class);
            BigDecimal sales = Optional.ofNullable(t.get(2, BigDecimal.class)).orElse(BigDecimal.ZERO);
            out.put(new SidLabelKey(sid, lbl), sales);
        }
        return out;
    }


    @Override
    public List<OrdersCardsDto> findOrdersSummary() {
        // 주문 카드 요약 조회 (올해시작부터 전일까지 누적 집계) - orders.html card 데이터
        return List.of();
    }

    @Override
    public List<MaterialsCardsDto> findMaterialsSummary() {
        // 재료 카드 요약 조회 (올해시작부터 전일까지 누적 집계) - materials.html card 데이터
        return List.of();
    }

    @Override
    public List<TimeCardsDto> findTimeSlicesSummary() {
        // 시간대,요일 카드 요약 조회 (올해시작부터 전일까지 누적 집계) - kpi.html card 데이터
        return List.of();
    }

    @Override
    public List<OrdersRowDto> findOrders(AnalyticsSearchDto analyticsSearchDto) {
        // 주문 분석 목록(카테고리/메뉴/주문형태 등) 조회 - orders.html 테이블 데이터
        return List.of();
    }

    @Override
    public List<MaterialsRowDto> findMaterials(AnalyticsSearchDto analyticsSearchDto) {
        // 재료 분석 목록(재고/발주/소진/원가/마진 등) 조회 - materials.html 테이블 데이터
        return List.of();
    }

    @Override
    public List<TimeRowDto> findTimeSlices(AnalyticsSearchDto analyticsSearchDto) {
        // 시간 기반 분석 목록(시간대/요일 등 슬라이스) 조회 - time.html 테이블 데이터
        return List.of();
    }
}
