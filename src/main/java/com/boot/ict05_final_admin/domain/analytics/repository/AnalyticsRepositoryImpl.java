package com.boot.ict05_final_admin.domain.analytics.repository;

import com.boot.ict05_final_admin.domain.analytics.dto.*;
import com.boot.ict05_final_admin.domain.order.entity.OrderStatus;
import com.boot.ict05_final_admin.domain.order.entity.OrderType;
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
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;

/**
 * AnalyticsRepositoryImpl (정리본)
 *
 * 제약: 배치/스케줄링/요약테이블 금지, Native SQL 금지, MyBatis/jOOQ 금지
 * 전략: 모든 집계/정렬/그룹핑은 DB에서 수행. 애플리케이션은 작은 결과만 수신.
 */
@Repository
@RequiredArgsConstructor
public class AnalyticsRepositoryImpl implements AnalyticsRepository {

    private final JPAQueryFactory query;

    // Q-classes
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

    /* =========================================================
       공통: 점포 옵션
       ========================================================= */
    @Override
    @Transactional(readOnly = true)
    public List<StoreOptionDto> findStoreOptions() {
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

    /* =========================================================
       KPI 요약 카드 (YTD/MTD/PMT/LYTD + 채널비율 + UPT/ADS/AUR)
       왕복 2회 (co 기반 집계 1회 + cod 수량 1회)
       ========================================================= */
    @Override
    @Transactional(readOnly = true)
    public KpiCardsDto findKpiSummary() {

        final var today     = LocalDate.now(ZONE_SEOUL);
        final var ytdStart  = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd    = today.minusDays(1);

        final var mtdStart  = today.withDayOfMonth(1);
        final var mtdEnd    = ytdEnd;

        final long dayCount = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(mtdStart, mtdEnd) + 1);
        final var pmtStart  = mtdStart.minusMonths(1);
        var       pmtEnd    = pmtStart.plusDays(Math.max(0, dayCount - 1));
        final var pmtLast   = pmtStart.plusMonths(1).minusDays(1);
        if (pmtEnd.isAfter(pmtLast)) pmtEnd = pmtLast;

        final var lytdStart = ytdStart.minusYears(1);
        final var lytdEnd   = ytdEnd.minusYears(1);

        final BooleanExpression done = co.status.eq(OrderStatus.COMPLETED);

        // (1) co 기반 모든 금액/건수/채널 집계
        Tuple t = query
                .select(
                        // YTD
                        sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd), co.totalPrice),           // 0
                        countIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd)),                         // 1
                        // MTD / PMT
                        sumIf(betweenDateClosedOpen(co.orderedAt, mtdStart, mtdEnd), co.totalPrice),           // 2
                        sumIf(betweenDateClosedOpen(co.orderedAt, pmtStart, pmtEnd), co.totalPrice),           // 3
                        // LYTD
                        sumIf(betweenDateClosedOpen(co.orderedAt, lytdStart, lytdEnd), co.totalPrice),         // 4
                        // Channel(YTD)
                        sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd).and(co.orderType.eq(OrderType.VISIT)),    co.totalPrice), // 5
                        sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd).and(co.orderType.eq(OrderType.TAKEOUT)),  co.totalPrice), // 6
                        sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd).and(co.orderType.eq(OrderType.DELIVERY)), co.totalPrice)  // 7
                )
                .from(co)
                .where(
                        done,
                        // 필요 기간만 OR 스캔
                        betweenDateClosedOpen(co.orderedAt, lytdStart, lytdEnd)
                                .or(betweenDateClosedOpen(co.orderedAt, pmtStart, pmtEnd))
                                .or(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd))
                )
                .fetchOne();

        BigDecimal ytdSales  = t != null ? nz(t.get(0, BigDecimal.class)) : BigDecimal.ZERO;
        long       ytdTrx    = t != null ? nz(t.get(1, Long.class))       : 0L;
        BigDecimal mtdSales     = t != null ? nz(t.get(2, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal pmtSales     = t != null ? nz(t.get(3, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal lytdSales    = t != null ? nz(t.get(4, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal visitSales   = t != null ? nz(t.get(5, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal takeoutSales = t != null ? nz(t.get(6, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal deliverySales= t != null ? nz(t.get(7, BigDecimal.class)) : BigDecimal.ZERO;

        // (2) cod 기반 YTD 수량
        Integer ytdUnitsI = Optional.ofNullable(
                query.select(cod.quantity.sum())
                        .from(cod).join(cod.order, co)
                        .where(done, betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd))
                        .fetchOne()
        ).orElse(0);
        BigDecimal ytdUnits = BigDecimal.valueOf(ytdUnitsI.longValue());

        BigDecimal trxBd = ytdTrx > 0 ? BigDecimal.valueOf(ytdTrx) : BigDecimal.ZERO;

        BigDecimal ads = divOrZero(ytdSales, trxBd, 2);   // 원/건
        BigDecimal upt = divOrZero(ytdUnits, trxBd, 6);   // 개/건
        BigDecimal aur = divOrZero(ytdSales, ytdUnits, 2);// 원/개

        BigDecimal compMoM = (pmtSales.signum()==0) ? BigDecimal.ZERO
                : mtdSales.divide(pmtSales, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);

        BigDecimal compYoY = (lytdSales.signum()==0) ? BigDecimal.ZERO
                : ytdSales.divide(lytdSales, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);

        BigDecimal visitR = (ytdSales.signum()==0) ? BigDecimal.ZERO : visitSales.multiply(BigDecimal.valueOf(100)).divide(ytdSales, 1, RoundingMode.HALF_UP);
        BigDecimal takeR  = (ytdSales.signum()==0) ? BigDecimal.ZERO : takeoutSales.multiply(BigDecimal.valueOf(100)).divide(ytdSales, 1, RoundingMode.HALF_UP);
        BigDecimal delivR = (ytdSales.signum()==0) ? BigDecimal.ZERO : deliverySales.multiply(BigDecimal.valueOf(100)).divide(ytdSales, 1, RoundingMode.HALF_UP);

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

    /* =========================================================
       KPI 목록 (일/월) : DB 페이징 + 선택 매장별/전사 Comp 계산
       ========================================================= */
    @Override
    @Transactional(readOnly = true)
    public Page<KpiRowDto> findKpi(AnalyticsSearchDto cond, Pageable pageable) {
        boolean byMonth = cond.getViewBy() == ViewBy.MONTH;
        String fmt = byMonth ? "%Y-%m" : "%Y-%m-%d";
        StringExpression labelExpr = dateFormat(co.orderedAt, fmt);

        BooleanExpression baseFilter = eqKpiFilter(cond, co, s);

        // total: 그룹 로우 수 (DB에서 스칼라로)
        StringExpression groupKey = Expressions.stringTemplate(
                "CONCAT_WS('|',{0},{1})", s.id, labelExpr
        );
        Long total = Optional.ofNullable(
                query.select(Expressions.numberTemplate(Long.class, "COUNT(DISTINCT {0})", groupKey))
                        .from(co).join(co.storeIdFk, s)
                        .where(baseFilter)
                        .fetchOne()
        ).orElse(0L);

        if (total == 0L) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0L);
        }

        // content
        List<Tuple> baseRows = query
                .select(
                        s.id, s.name,
                        labelExpr,
                        co.totalPrice.sum(),      // 매출 (중복 없음)
                        co.id.countDistinct()     // 거래수는 DISTINCT로
                )
                .from(co)
                .join(co.storeIdFk, s)
                .where(baseFilter)
                .groupBy(s.id, s.name, labelExpr)
                .orderBy(labelExpr.desc(), co.totalPrice.sum().coalesce(BigDecimal.ZERO).desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Tuple> unitRows = query
                .select(
                        s.id,
                        labelExpr,
                        cod.quantity.sum()
                )
                .from(cod)
                .join(cod.order, co)
                .join(co.storeIdFk, s)
                .where(baseFilter)
                .groupBy(s.id, labelExpr)
                .fetch();

        record Key(Long sid, String label) {}
        Map<Key, Integer> unitsMap = new HashMap<>();
        for (Tuple t : unitRows) {
            unitsMap.put(new Key(t.get(0, Long.class), t.get(1, String.class)),
                    nz(t.get(2, Integer.class)));
        }

        if (baseRows.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }

        // 페이지에 보이는 점포 id set
        Set<Long> pageSids = new HashSet<>();
        for (Tuple t : baseRows) pageSids.add(t.get(0, Long.class));

        List<Long> selected = cond.getStoreIds();
        boolean isAll = (selected == null || selected.isEmpty());
        boolean isMulti = (selected != null && selected.size() > 1);

        Map<Long, GlobalComp> perStoreComp = null;
        GlobalComp globalComp = null;

        if (isAll || isMulti) {
            Set<Long> targetSids = isAll ? pageSids : new HashSet<>(selected);
            perStoreComp = computeCompByStore(targetSids);
        } else {
            globalComp = computeGlobalComp(cond); // 단일 점포
        }

        List<KpiRowDto> content = new ArrayList<>(baseRows.size());
        for (Tuple t : baseRows) {
            Long sid       = t.get(0, Long.class);
            String sname   = t.get(1, String.class);
            String label   = t.get(2, String.class);
            BigDecimal sales = nz(t.get(3, BigDecimal.class));
            long trx        = nz(t.get(4, Long.class));

            Integer unitsI  = unitsMap.getOrDefault(new Key(sid, label), 0);
            BigDecimal units = BigDecimal.valueOf(unitsI.longValue());
            BigDecimal trxBd = trx > 0 ? BigDecimal.valueOf(trx) : BigDecimal.ZERO;

            BigDecimal ads = divOrZero(sales, trxBd, 2);
            BigDecimal upt = divOrZero(units, trxBd, 6);
            BigDecimal aur = divOrZero(sales, units, 2);

            GlobalComp compVal = (isAll || isMulti)
                    ? perStoreComp.getOrDefault(sid, GlobalComp.ZERO)
                    : (globalComp == null ? GlobalComp.ZERO : globalComp);

            content.add(KpiRowDto.builder()
                    .date(label)
                    .storeName(sname)
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

    /* =========================================================
       주문 요약 카드 (YTD) : 상세 로우 가져오지 않음, DB 집계/TopN만 수신
       ========================================================= */
    @Override
    @Transactional(readOnly = true)
    public OrdersCardsDto findOrdersSummary() {
        final var today    = LocalDate.now(ZONE_SEOUL);
        final var ytdStart = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd   = today.minusDays(1);

        final BooleanExpression ytd = co.status.eq(OrderStatus.COMPLETED)
                .and(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd));

        // (A) 주문(co) 단일 스캔: 총액/거래수/채널액
        Tuple t = query
                .select(
                        sumIf(ytd, co.totalPrice),                                                             // 0
                        countIf(ytd),                                                                          // 1
                        sumIf(ytd.and(co.orderType.eq(OrderType.VISIT)),    co.totalPrice),                    // 2
                        sumIf(ytd.and(co.orderType.eq(OrderType.TAKEOUT)),  co.totalPrice),                    // 3
                        sumIf(ytd.and(co.orderType.eq(OrderType.DELIVERY)), co.totalPrice)                     // 4
                )
                .from(co)
                .fetchOne();

        BigDecimal totalSales = t != null ? nz(t.get(0, BigDecimal.class)) : BigDecimal.ZERO;
        long       trx        = t != null ? nz(t.get(1, Long.class))       : 0L;
        BigDecimal visit      = t != null ? nz(t.get(2, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal takeout    = t != null ? nz(t.get(3, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal delivery   = t != null ? nz(t.get(4, BigDecimal.class)) : BigDecimal.ZERO;

        // (B) Top1 카테고리 (co 시작)
        Tuple topCat = query
                .select(mc.menuCategoryName, cod.lineTotal.sum())
                .from(co)
                .join(cod).on(cod.order.eq(co))
                .join(cod.menuIdFk, m)
                .join(m.menuCategory, mc)
                .where(ytd)
                .groupBy(mc.menuCategoryId, mc.menuCategoryName)
                .orderBy(cod.lineTotal.sum().desc())
                .fetchFirst();

        String     topCategoryName  = topCat == null ? null : topCat.get(0, String.class);
        BigDecimal topCategorySales = topCat == null ? BigDecimal.ZERO : nz(topCat.get(1, BigDecimal.class));

        // (C) Top3 메뉴 (co 시작)
        List<Tuple> topMenusT = query
                .select(m.menuId, m.menuName, cod.quantity.sum(), cod.lineTotal.sum())
                .from(co)
                .join(cod).on(cod.order.eq(co))
                .join(cod.menuIdFk, m)
                .where(ytd)
                .groupBy(m.menuId, m.menuName)
                .orderBy(cod.lineTotal.sum().desc())
                .limit(3)
                .fetch();

        List<TopMenuItem> topMenus = new ArrayList<>(topMenusT.size());
        BigDecimal top3Sum = BigDecimal.ZERO;
        for (Tuple r : topMenusT) {
            Long       mid    = r.get(0, Long.class);
            String     mname  = r.get(1, String.class);
            Long       qty    = Optional.ofNullable(r.get(2, Integer.class)).map(Integer::longValue).orElse(0L);
            BigDecimal msales = nz(r.get(3, BigDecimal.class));
            top3Sum = top3Sum.add(msales);
            topMenus.add(TopMenuItem.builder().menuId(mid).menuName(mname).quantity(qty).sales(msales).build());
        }

        // (D) 라인매출/카테고리수/메뉴수 한방 (co 시작)
        Tuple denom = query
                .select(
                        cod.lineTotal.sum(),               // 0: total line sales
                        mc.menuCategoryId.countDistinct(), // 1: category count
                        m.menuId.countDistinct()           // 2: menu count
                )
                .from(co)
                .join(cod).on(cod.order.eq(co))
                .join(cod.menuIdFk, m)
                .join(m.menuCategory, mc)
                .where(ytd)
                .fetchOne();

        BigDecimal totalLineSales = denom != null ? nz(denom.get(0, BigDecimal.class)) : BigDecimal.ZERO;
        long categoryCount        = denom != null ? nz(denom.get(1, Long.class))       : 0L;
        long menuCount            = denom != null ? nz(denom.get(2, Long.class))       : 0L;

        BigDecimal topCategoryRatio = divOrZero(topCategorySales.multiply(BigDecimal.valueOf(100)), totalLineSales, 1);
        BigDecimal menuTop3Ratio    = divOrZero(top3Sum.multiply(BigDecimal.valueOf(100)),         totalLineSales, 1);

        return OrdersCardsDto.builder()
                .transaction(trx)
                .visitSales(visit)
                .takeoutSales(takeout)
                .deliverySales(delivery)
                .categoryCount(categoryCount)
                .categoryName(topCategoryName)
                .categoryRatio(topCategoryRatio)
                .menuCount(menuCount)
                .menuTop3Ratio(menuTop3Ratio)
                .topMenus(topMenus)
                .build();
    }



    /* =========================================================
       주문 목록 (카테고리/메뉴/주문형태 등) : 정확한 total + DB 페이징
       버킷(점포×라벨×주문형태) "주문건수/주문매출"은 별도 한방쿼리
       ========================================================= */
    @Override
    @Transactional(readOnly = true)
    public Page<OrdersRowDto> findOrders(AnalyticsSearchDto cond, Pageable pageable) {
        boolean byMonth = cond.getViewBy() == ViewBy.MONTH;
        String  fmt     = byMonth ? "%Y-%m" : "%Y-%m-%d";
        StringExpression labelExpr = dateFormat(co.orderedAt, fmt);

        // 공통 필터
        BooleanExpression filter = co.status.eq(OrderStatus.COMPLETED);
        if (cond.getStartDate() != null || cond.getEndDate() != null) {
            filter = filter.and(betweenDateClosedOpen(co.orderedAt, cond.getStartDate(), cond.getEndDate()));
        }
        if (cond.getStoreIds() != null && !cond.getStoreIds().isEmpty()) {
            filter = filter.and(s.id.in(cond.getStoreIds()));
        }

        // (1) total (그룹키: 점포/카테고리/메뉴/라벨/주문형태)
        StringExpression groupKey = Expressions.stringTemplate(
                "CONCAT_WS('|',{0},{1},{2},{3},{4})",
                s.id, mc.menuCategoryId, m.menuId, labelExpr, co.orderType.stringValue()
        );

        Long total = Optional.ofNullable(
                query.select(Expressions.numberTemplate(Long.class, "COUNT(DISTINCT {0})", groupKey))
                        .from(co)
                        .join(cod).on(cod.order.eq(co))
                        .join(co.storeIdFk, s)
                        .join(cod.menuIdFk, m)
                        .join(m.menuCategory, mc)
                        .where(filter)
                        .fetchOne()
        ).orElse(0L);

        if (total == 0L) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0L);
        }

        // (2) 페이지 컨텐트 (cod 기반: 메뉴수량/라인매출)
        List<Tuple> rows = query
                .select(
                        s.id, s.name,                     // 0,1
                        mc.menuCategoryName,              // 2
                        m.menuName,                       // 3
                        labelExpr,                        // 4
                        co.orderType,                     // 5
                        cod.quantity.sum(),               // 6
                        cod.lineTotal.sum()               // 7
                )
                .from(cod)
                .join(cod.order, co)
                .join(co.storeIdFk, s)
                .join(cod.menuIdFk, m)
                .join(m.menuCategory, mc)
                .where(filter)
                .groupBy(s.id, s.name, mc.menuCategoryName, m.menuName, labelExpr, co.orderType)
                .orderBy(
                        labelExpr.desc(),
                        cod.lineTotal.sum().coalesce(BigDecimal.ZERO).desc(),
                        s.id.asc(), m.menuName.asc() // tie-breaker 안정성
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (rows.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }

        // (3) 버킷 집합 수집 (sid, label, type)
        Set<Long> sids = new HashSet<>();
        Set<String> labels = new HashSet<>();
        Set<OrderType> types = new HashSet<>();
        for (Tuple r : rows) {
            sids.add(r.get(0, Long.class));
            labels.add(r.get(4, String.class));
            OrderType ot = r.get(5, OrderType.class);
            if (ot != null) types.add(ot);
        }

        // KPI와 동일하게 labelExpr 재사용
        StringExpression labelForAgg = dateFormat(co.orderedAt, fmt);

        // (4) 버킷 집계(co 기반: 주문건수/주문총액) — 한방
        List<Tuple> bucketAgg = query
                .select(
                        s.id,
                        labelForAgg,
                        co.orderType,
                        co.id.countDistinct(),    // 주문 건수
                        co.totalPrice.sum()       // 주문 매출(주문총액)
                )
                .from(co)
                .join(co.storeIdFk, s)
                .where(
                        co.status.eq(OrderStatus.COMPLETED),
                        (cond.getStartDate()!=null || cond.getEndDate()!=null)
                                ? betweenDateClosedOpen(co.orderedAt, cond.getStartDate(), cond.getEndDate())
                                : null,
                        (cond.getStoreIds()!=null && !cond.getStoreIds().isEmpty())
                                ? s.id.in(cond.getStoreIds())
                                : null,
                        s.id.in(sids),
                        labelForAgg.in(labels),
                        !types.isEmpty() ? co.orderType.in(new ArrayList<>(types)) : null
                )
                .groupBy(s.id, labelForAgg, co.orderType)
                .fetch();

        // (5) 매핑
        record Key(Long sid, String label, OrderType type) {}
        Map<Key, Tuple> bucketMap = new HashMap<>();
        for (Tuple r : bucketAgg) {
            bucketMap.put(new Key(r.get(0, Long.class), r.get(1, String.class), r.get(2, OrderType.class)), r);
        }

        List<OrdersRowDto> content = new ArrayList<>(rows.size());
        for (Tuple r : rows) {
            Long sid        = r.get(0, Long.class);
            String sname    = r.get(1, String.class);
            String catNm    = r.get(2, String.class);
            String menuNm   = r.get(3, String.class);
            String label    = r.get(4, String.class);
            OrderType ot    = r.get(5, OrderType.class);

            Long menuQty        = Optional.ofNullable(r.get(6, Integer.class)).map(Integer::longValue).orElse(0L);
            BigDecimal menuSales= nz(r.get(7, BigDecimal.class));

            Tuple b = bucketMap.getOrDefault(new Key(sid, label, ot), null);
            Long orderCnt        = (b==null) ? 0L : nz(b.get(3, Long.class));
            BigDecimal orderSales= (b==null) ? BigDecimal.ZERO : nz(b.get(4, BigDecimal.class));

            content.add(OrdersRowDto.builder()
                    .date(label)
                    .storeName(sname)
                    .category(catNm)
                    .menu(menuNm)
                    .menuCount(menuQty)
                    .menuSales(menuSales)
                    .orderCount(orderCnt)
                    .orderSales(orderSales)
                    .orderType(ot != null ? ot.name() : null)
                    .orderDate(label)
                    .build());
        }

        return new PageImpl<>(content, pageable, total);
    }
    /* =========================================================
       재료 요약 카드 / 목록
       - HQ 재고(office)는 별도 테이블이 없다면 0으로 두고 훅만 제공
       - 현재고는 store_inventory 합계, 발주는 receive_order_detail 합계
       ========================================================= */
    @Override
    @Transactional(readOnly = true)
    public MaterialsCardsDto findMaterialsSummary(AnalyticsSearchDto cond) {

        long officeQty = 0L; // HQ 재고는 후속 연결 전까지 0

        BooleanExpression storeFilter =
                (cond.getStoreIds()!=null && !cond.getStoreIds().isEmpty()) ? s.id.in(cond.getStoreIds()) : null;

        // 1) 점포 현재고 합 (store_inventory) — BigDecimal로 받아서 long으로 변환
        BigDecimal storeQtyBd = Optional.ofNullable(
                query.select(si.quantity.sum())
                        .from(si)
                        .join(si.store, s)
                        .where(storeFilter)
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);
        long storeQty = storeQtyBd.longValue();

        // 2) 발주 수량 합 (기간) — 필드명이 달라도 동작 (quantity 기반)
        BooleanExpression period = betweenDateClosedOpen(ro.actualDeliveryDate, cond.getStartDate(), cond.getEndDate());

        // 만약 rod.quantity 필드명이 다르면 Q클래스 기준으로 바꿔 주세요.
        Integer orderVolI = Optional.ofNullable(
                query.select(rod.detailCount.sum())
                        .from(rod)
                        .join(rod.receiveOrder, ro)
                        .join(ro.store, s)
                        .where(storeFilter, period)
                        .fetchOne()
        ).orElse(0);

        return MaterialsCardsDto.builder()
                .currentOfficeInventoryQty(officeQty)
                .currentTotalStoreInventoryQty(storeQty)
                .orderVolumeQty(orderVolI.longValue())
                .usedQty(0L)                   // Phase A
                .turnoverRate(BigDecimal.ZERO) // Phase A
                .salesAmount(BigDecimal.ZERO)  // Phase A
                .profitAmount(BigDecimal.ZERO) // Phase A
                .avgMargin(BigDecimal.ZERO)    // Phase A
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialsRowDto> findMaterials(AnalyticsSearchDto cond) {
        boolean byMonth = cond.getViewBy() == ViewBy.MONTH;
        String fmt = byMonth ? "%Y-%m" : "%Y-%m-%d";

        // 라벨은 실제 납품일 기준
        StringExpression labelExpr = dateFormat(ro.actualDeliveryDate, fmt);

        BooleanExpression storeFilter =
                (cond.getStoreIds()!=null && !cond.getStoreIds().isEmpty()) ? s.id.in(cond.getStoreIds()) : null;
        BooleanExpression period = betweenDateClosedOpen(ro.actualDeliveryDate, cond.getStartDate(), cond.getEndDate());

        // (A) 발주 집계
        //  - 수량: rod.quantity
        //  - 금액: quantity * unit_price 의 합으로 안전 계산 (detailTotalPrice 없을 때도 동작)
        NumberExpression<BigDecimal> orderAmountExpr =
                Expressions.numberTemplate(
                        BigDecimal.class,
                        "SUM({0} * {1})",
                        rod.detailCount, rod.detailUnitPrice
                );

        List<Tuple> rows = query
                .select(
                        labelExpr,                // 0
                        s.id,                     // 1
                        s.name,                   // 2
                        mat.id,                   // 3
                        mat.name,                 // 4
                        rod.detailCount.sum(),       // 5
                        orderAmountExpr           // 6
                )
                .from(rod)
                .join(rod.receiveOrder, ro)
                .join(ro.store, s)
                // .join(rod.storeMaterial, sm)
                // .join(sm.material, mat)
                .join(rod.material, mat)
                .where(storeFilter, period)
                .groupBy(labelExpr, s.id, s.name, mat.id, mat.name)
                .orderBy(labelExpr.desc(), orderAmountExpr.desc())
                .fetch();

        // (B) 현재고 (해당 (store, material) 조합만)
        Map<String, Long> onhand = new HashMap<>();
        if (!rows.isEmpty()) {
            Set<Long> sids = new HashSet<>(), mids = new HashSet<>();
            for (Tuple t : rows) { sids.add(t.get(1, Long.class)); mids.add(t.get(3, Long.class)); }

            List<Tuple> inv = query
                    .select(s.id, mat.id, si.quantity.sum())
                    .from(si)
                    .join(si.store, s)
                    .join(si.storeMaterial, sm)
                    .join(sm.material, mat)
                    .where(
                            s.id.in(sids),
                            mat.id.in(mids),
                            (cond.getStoreIds()!=null && !cond.getStoreIds().isEmpty()) ? s.id.in(cond.getStoreIds()) : null
                    )
                    .groupBy(s.id, mat.id)
                    .fetch();

            for (Tuple t : inv) {
                String key = t.get(0, Long.class) + ":" + t.get(1, Long.class);
                BigDecimal q = Optional.ofNullable(t.get(2, BigDecimal.class)).orElse(BigDecimal.ZERO);
                onhand.put(key, q.longValue()); // ✅ BigDecimal → long
            }
        }

        long dayCount = (cond.getStartDate()!=null && cond.getEndDate()!=null)
                ? Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(cond.getStartDate(), cond.getEndDate()) + 1)
                : 1;

        List<MaterialsRowDto> out = new ArrayList<>(rows.size());
        for (Tuple t : rows) {
            String label   = t.get(0, String.class);
            Long   sid     = t.get(1, Long.class);
            String sname   = t.get(2, String.class);
            Long   mid     = t.get(3, Long.class);
            String mname   = t.get(4, String.class);

            Long oQty = Optional.ofNullable(t.get(5, Integer.class)).map(Integer::longValue).orElse(0L);
            BigDecimal oAmt = Optional.ofNullable(t.get(6, BigDecimal.class)).orElse(BigDecimal.ZERO);

            Long onhandQty = onhand.getOrDefault(sid + ":" + mid, 0L);

            out.add(MaterialsRowDto.builder()
                    .orderDate(label)
                    .store(sname)
                    .material(mname)
                    .storeInventoryQty(onhandQty)
                    .orderAmount(oAmt)
                    .turnoverRate(BigDecimal.ZERO)  // Phase A
                    .profit(BigDecimal.ZERO)        // Phase A
                    .margin(BigDecimal.ZERO)        // Phase A
                    .avgDailyUsage(dayCount>0
                            ? new BigDecimal(oQty).divide(new BigDecimal(dayCount), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO)
                    .storeId(sid)
                    .materialId(mid)
                    .build());
        }
        return out;
    }


    /* =========================================================
       (향후) 시간대/요일 슬라이스 (Phase A: 스텁)
       ========================================================= */
    @Override
    @Transactional(readOnly = true)
    public List<TimeCardsDto> findTimeSlicesSummary() {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeRowDto> findTimeSlices(AnalyticsSearchDto cond) {
        return List.of();
    }

    /* ===================== Helper Methods ===================== */

    /** KPI/목록 공통 필터 */
    private BooleanExpression eqKpiFilter(AnalyticsSearchDto cond, QCustomerOrder co, QStore s) {
        BooleanExpression where = co.status.eq(OrderStatus.COMPLETED);
        if (cond.getStartDate() != null && cond.getEndDate() != null) {
            where = where.and(betweenDateClosedOpen(co.orderedAt, cond.getStartDate(), cond.getEndDate()));
        }
        List<Long> storeIds = cond.getStoreIds();
        if (storeIds != null && !storeIds.isEmpty()) {
            where = where.and(s.id.in(storeIds));
        }
        return where;
    }

    /** [start 00:00:00, end+1 00:00:00) — 인덱스 사용 가능 */
    private BooleanExpression betweenDateClosedOpen(DateTimePath<LocalDateTime> path,
                                                    LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            return path.goe(start.atStartOfDay()).and(path.lt(end.plusDays(1).atStartOfDay()));
        } else if (start != null) {
            return path.goe(start.atStartOfDay());
        } else if (end != null) {
            return path.lt(end.plusDays(1).atStartOfDay());
        } else {
            return null;
        }
    }

    private BooleanExpression betweenDateClosedOpen(DatePath<LocalDate> path,
                                                    LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            return path.goe(start).and(path.lt(end.plusDays(1)));
        } else if (start != null) {
            return path.goe(start);
        } else if (end != null) {
            return path.lt(end.plusDays(1));
        } else {
            return null;
        }
    }

    /** MariaDB DATE_FORMAT(date, fmt) */
    private StringExpression dateFormat(Expression<?> dateTime, String fmt) {
        return Expressions.stringTemplate("DATE_FORMAT({0}, {1})", dateTime, Expressions.constant(fmt));
    }

    /** SUM(CASE WHEN cond THEN value ELSE 0 END) */
    private NumberExpression<BigDecimal> sumIf(BooleanExpression condition, NumberExpression<BigDecimal> value) {
        return Expressions.numberTemplate(
                BigDecimal.class,
                "SUM(CASE WHEN {0} THEN {1} ELSE CAST(0 AS DECIMAL(20,6)) END)",
                condition, value
        );
    }

    /** SUM(CASE WHEN cond THEN 1 ELSE 0 END) */
    private NumberExpression<Long> countIf(BooleanExpression condition) {
        return Expressions.numberTemplate(Long.class, "SUM(CASE WHEN {0} THEN 1 ELSE 0 END)", condition);
    }

    /** 0-나눗셈 보호 */
    private static BigDecimal divOrZero(BigDecimal num, BigDecimal den, int scale) {
        return (den == null || den.signum() == 0)
                ? BigDecimal.ZERO
                : num.divide(den, scale, RoundingMode.HALF_UP);
    }

    /** nvl for wrapper types */
    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private static Long       nz(Long v)       { return v == null ? 0L : v; }
    private static Integer    nz(Integer v)    { return v == null ? 0 : v; }

    /** 단일 점포 선택 시 전사 대신 단일 집계(한방) */
    private GlobalComp computeGlobalComp(AnalyticsSearchDto cond) {
        BooleanExpression base = co.status.eq(OrderStatus.COMPLETED);
        if (cond.getStoreIds() != null && !cond.getStoreIds().isEmpty()) {
            base = base.and(s.id.in(cond.getStoreIds()));
        }

        final var today     = LocalDate.now(ZONE_SEOUL);
        final var mtdStart  = today.withDayOfMonth(1);
        final var mtdEnd    = today.minusDays(1);
        final long dayCount = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(mtdStart, mtdEnd) + 1);

        final var pmtStart  = mtdStart.minusMonths(1);
        var       pmtEnd    = pmtStart.plusDays(Math.max(0, dayCount - 1));
        final var pmtLast   = pmtStart.plusMonths(1).minusDays(1);
        if (pmtEnd.isAfter(pmtLast)) pmtEnd = pmtLast;

        final var ytdStart  = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd    = today.minusDays(1);
        final var lytdStart = ytdStart.minusYears(1);
        final var lytdEnd   = ytdEnd.minusYears(1);

        Tuple t = query
                .select(
                        sumIf(betweenDateClosedOpen(co.orderedAt, mtdStart, mtdEnd), co.totalPrice),  // 0
                        sumIf(betweenDateClosedOpen(co.orderedAt, pmtStart, pmtEnd), co.totalPrice),  // 1
                        sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd), co.totalPrice),  // 2
                        sumIf(betweenDateClosedOpen(co.orderedAt, lytdStart, lytdEnd), co.totalPrice) // 3
                )
                .from(co)
                .join(co.storeIdFk, s)
                .where(base)
                .fetchOne();

        BigDecimal mtd = t != null ? nz(t.get(0, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal pmt = t != null ? nz(t.get(1, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal ytd = t != null ? nz(t.get(2, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal ly  = t != null ? nz(t.get(3, BigDecimal.class)) : BigDecimal.ZERO;

        BigDecimal mom = (pmt.signum()==0) ? BigDecimal.ZERO
                : mtd.divide(pmt, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
        BigDecimal yoy = (ly.signum()==0) ? BigDecimal.ZERO
                : ytd.divide(ly, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));

        return new GlobalComp(mom, yoy);
    }

    /** 여러 점포 선택/전사일 때 페이지에 노출된 점포들에 대해 점포별 Comp (한방) */
    private Map<Long, GlobalComp> computeCompByStore(Set<Long> sidsOnPage) {
        Map<Long, GlobalComp> out = new HashMap<>();
        if (sidsOnPage == null || sidsOnPage.isEmpty()) return out;

        final var today     = LocalDate.now(ZONE_SEOUL);
        final var mtdStart  = today.withDayOfMonth(1);
        final var mtdEnd    = today.minusDays(1);
        final long dayCount = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(mtdStart, mtdEnd) + 1);

        final var pmtStart  = mtdStart.minusMonths(1);
        var       pmtEnd    = pmtStart.plusDays(Math.max(0, dayCount - 1));
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
            BigDecimal mtd = nz(t.get(1, BigDecimal.class));
            BigDecimal pmt = nz(t.get(2, BigDecimal.class));
            BigDecimal ytd = nz(t.get(3, BigDecimal.class));
            BigDecimal ly  = nz(t.get(4, BigDecimal.class));

            BigDecimal mom = (pmt.signum()==0) ? BigDecimal.ZERO
                    : mtd.divide(pmt, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
            BigDecimal yoy = (ly.signum()==0) ? BigDecimal.ZERO
                    : ytd.divide(ly, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));

            out.put(sid, new GlobalComp(mom, yoy));
        }
        return out;
    }

    /* ===================== 내부용 구조체 ===================== */
    private static class GlobalComp {
        static final GlobalComp ZERO = new GlobalComp(BigDecimal.ZERO, BigDecimal.ZERO);
        final BigDecimal compMoM;
        final BigDecimal compYoY;
        GlobalComp(BigDecimal mom, BigDecimal yoy) { this.compMoM = mom; this.compYoY = yoy; }
    }
}
