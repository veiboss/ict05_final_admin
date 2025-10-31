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
import com.querydsl.jpa.impl.JPAQuery;
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
 * AnalyticsRepositoryImpl (지침 반영본)
 * - 기간 조건: [start 00:00, end+1 00:00) 고정
 * - OR 범위 금지 → 한 번의 넓은 기간 WHERE + CASE 집계
 * - 애플리케이션에서만 파생지표 계산(UPT/ADS/AUR/Comp)
 * - 모든 SELECT에 readOnly/timeout 힌트 적용
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
        return readHints(
                query.select(Projections.constructor(StoreOptionDto.class, s.id, s.name))
                        .from(s)
                        .orderBy(s.name.asc())
        ).fetch();
    }

    /* ===================== KPI 요약 카드 ===================== */
    @Override
    @Transactional(readOnly = true)
    public KpiCardsDto findKpiSummary() {
        final var today    = LocalDate.now(ZONE_SEOUL);
        final var ytdStart = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd   = today.minusDays(1);

        // ✅ MoM 윈도우 적용 (1일 예외 포함)
        MoMWindows w = calcMoMWindows(today);
        final var aStart = w.aStart();  // MTD or 전월 전체
        final var aEnd   = w.aEnd();
        final var bStart = w.bStart();  // PMT or 전전월 전체
        final var bEnd   = w.bEnd();

        final var lytdStart = ytdStart.minusYears(1);
        final var lytdEnd   = ytdEnd.minusYears(1);

        final BooleanExpression done = co.status.eq(OrderStatus.COMPLETED);

        Tuple t = readHints(
                query.select(
                                // YTD
                                sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd), co.totalPrice),
                                countIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd)),

                                // ✅ A / B (MoM 양 구간)
                                sumIf(betweenDateClosedOpen(co.orderedAt, aStart, aEnd), co.totalPrice),
                                sumIf(betweenDateClosedOpen(co.orderedAt, bStart, bEnd), co.totalPrice),

                                // LYTD
                                sumIf(betweenDateClosedOpen(co.orderedAt, lytdStart, lytdEnd), co.totalPrice),

                                // Channel(YTD)
                                sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd).and(co.orderType.eq(OrderType.VISIT)),    co.totalPrice),
                                sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd).and(co.orderType.eq(OrderType.TAKEOUT)),  co.totalPrice),
                                sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd).and(co.orderType.eq(OrderType.DELIVERY)), co.totalPrice)
                        )
                        .from(co)
                        .where(done, betweenDateClosedOpen(co.orderedAt, lytdStart, ytdEnd))
        ).fetchOne();

        BigDecimal ytdSales   = t != null ? nz(t.get(0, BigDecimal.class)) : BigDecimal.ZERO;
        long       ytdTrx     = t != null ? nz(t.get(1, Long.class))       : 0L;
        BigDecimal aSales     = t != null ? nz(t.get(2, BigDecimal.class)) : BigDecimal.ZERO; // A
        BigDecimal bSales     = t != null ? nz(t.get(3, BigDecimal.class)) : BigDecimal.ZERO; // B
        BigDecimal lytdSales  = t != null ? nz(t.get(4, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal visitSales = t != null ? nz(t.get(5, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal takeSales  = t != null ? nz(t.get(6, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal delvSales  = t != null ? nz(t.get(7, BigDecimal.class)) : BigDecimal.ZERO;

        Long ytdUnitsL = Optional.ofNullable(
                readHints(
                        query.select(cod.quantity.sum().longValue())
                                .from(cod).join(cod.order, co)
                                .where(done, betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd))
                ).fetchOne()
        ).orElse(0L);

        BigDecimal ytdUnits = BigDecimal.valueOf(ytdUnitsL);
        BigDecimal trxBd    = ytdTrx > 0 ? BigDecimal.valueOf(ytdTrx) : BigDecimal.ZERO;

        BigDecimal ads = divOrZero(ytdSales, trxBd, 2);
        BigDecimal upt = divOrZero(ytdUnits, trxBd, 6);
        BigDecimal aur = divOrZero(ytdSales, ytdUnits, 2);

        BigDecimal compMoM = (bSales.signum() == 0) ? BigDecimal.ZERO
                : aSales.divide(bSales, 6, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);

        BigDecimal compYoY = (lytdSales.signum()==0) ? BigDecimal.ZERO
                : ytdSales.divide(lytdSales, 6, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);

        BigDecimal visitR = (ytdSales.signum()==0) ? BigDecimal.ZERO
                : visitSales.multiply(BigDecimal.valueOf(100)).divide(ytdSales, 1, RoundingMode.HALF_UP);
        BigDecimal takeR  = (ytdSales.signum()==0) ? BigDecimal.ZERO
                : takeSales.multiply(BigDecimal.valueOf(100)).divide(ytdSales, 1, RoundingMode.HALF_UP);
        BigDecimal delvR  = (ytdSales.signum()==0) ? BigDecimal.ZERO
                : delvSales.multiply(BigDecimal.valueOf(100)).divide(ytdSales, 1, RoundingMode.HALF_UP);

        return KpiCardsDto.builder()
                .sales(ytdSales).transaction(ytdTrx)
                .upt(upt).ads(ads).aur(aur)
                .compMoM(compMoM).compYoY(compYoY)
                .visitRatio(visitR).takeoutRatio(takeR).deliveryRatio(delvR)
                .build();
    }


    /* ===================== KPI 목록(일/월) ===================== */
    @Override
    @Transactional(readOnly = true)
    public Page<KpiRowDto> findKpi(AnalyticsSearchDto cond, Pageable pageable) {
        boolean byMonth = cond.getViewBy() == ViewBy.MONTH;
        String fmt = byMonth ? "%Y-%m" : "%Y-%m-%d";
        StringExpression labelExpr = dateFormat(co.orderedAt, fmt);

        BooleanExpression baseFilter = eqKpiFilter(cond, co, s);

        // total: DISTINCT (store|label) 개수
        StringExpression groupKey = Expressions.stringTemplate(
                "CONCAT_WS('|',{0},{1})", s.id, labelExpr
        );

        Long total = Optional.ofNullable(
                readHints(
                        query.select(Expressions.numberTemplate(Long.class, "COUNT(DISTINCT {0})", groupKey))
                                .from(co).join(co.storeIdFk, s)
                                .where(baseFilter)
                ).fetchOne()
        ).orElse(0L);

        if (total == 0L) return new PageImpl<>(Collections.emptyList(), pageable, 0L);

        // 본문: 매출/거래수
        List<Tuple> baseRows = readHints(
                query.select(
                                s.id, s.name,
                                labelExpr,
                                co.totalPrice.sum(),
                                co.id.countDistinct()
                        )
                        .from(co)
                        .join(co.storeIdFk, s)
                        .where(baseFilter)
                        .groupBy(s.id, s.name, labelExpr)
                        .orderBy(labelExpr.desc(), co.totalPrice.sum().coalesce(BigDecimal.ZERO).desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
        ).fetch();

        if (baseRows.isEmpty()) return new PageImpl<>(Collections.emptyList(), pageable, total);

        // 같은 필터로 수량 합
        List<Tuple> unitRows = readHints(
                query.select(
                                s.id,
                                labelExpr,
                                cod.quantity.sum()
                        )
                        .from(cod)
                        .join(cod.order, co)
                        .join(co.storeIdFk, s)
                        .where(baseFilter)
                        .groupBy(s.id, labelExpr)
        ).fetch();

        record Key(Long sid, String label) {}
        Map<Key, Integer> unitsMap = new HashMap<>();
        for (Tuple t : unitRows) {
            unitsMap.put(new Key(t.get(0, Long.class), t.get(1, String.class)),
                    nz(t.get(2, Integer.class)));
        }

        // Comp 계산: 단일점포 vs 다중/전체
        Set<Long> pageSids = new HashSet<>();
        for (Tuple t : baseRows) pageSids.add(t.get(0, Long.class));

        List<Long> selected = cond.getStoreIds();
        boolean isAll   = (selected == null || selected.isEmpty());
        boolean isMulti = (selected != null && selected.size() > 1);

        Map<Long, GlobalComp> perStoreComp = null;
        GlobalComp globalComp = null;
        if (isAll || isMulti) {
            perStoreComp = computeCompByStore(pageSids);
        } else {
            globalComp = computeGlobalComp(cond);
        }

        List<KpiRowDto> content = new ArrayList<>(baseRows.size());
        for (Tuple t : baseRows) {
            Long sid      = t.get(0, Long.class);
            String sname  = t.get(1, String.class);
            String label  = t.get(2, String.class);
            BigDecimal sales = nz(t.get(3, BigDecimal.class));
            long trx         = nz(t.get(4, Long.class));

            Integer unitsI    = unitsMap.getOrDefault(new Key(sid, label), 0);
            BigDecimal units  = BigDecimal.valueOf(unitsI.longValue());
            BigDecimal trxBd  = trx > 0 ? BigDecimal.valueOf(trx) : BigDecimal.ZERO;

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

        // YTD 조건
        final BooleanExpression ytd = co.status.eq(OrderStatus.COMPLETED)
                .and(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd));

        // (A) 주문(co) 단일 스캔: Bean Projection으로 일부 필드 즉시 주입
        OrdersCardsDto dto = readHints(
                query.select(Projections.bean(OrdersCardsDto.class,
                                // 주의: DTO의 필드명(Setter)과 alias를 반드시 일치시킴
                                countIf(ytd).as("transaction"),
                                sumIf(ytd.and(co.orderType.eq(OrderType.VISIT)),    co.totalPrice).as("visitSales"),
                                sumIf(ytd.and(co.orderType.eq(OrderType.TAKEOUT)),  co.totalPrice).as("takeoutSales"),
                                sumIf(ytd.and(co.orderType.eq(OrderType.DELIVERY)), co.totalPrice).as("deliverySales")
                        ))
                        .from(co)
        ).fetchOne();

        if (dto == null) dto = new OrdersCardsDto(); // NPE 방지

        // (B) 카테고리/메뉴 통계 (cod 기반)
        //  - Top1 카테고리
        Tuple topCat = readHints(
                query.select(mc.menuCategoryName, cod.lineTotal.sum())
                        .from(co)
                        .join(cod).on(cod.order.eq(co))
                        .join(cod.menuIdFk, m)
                        .join(m.menuCategory, mc)
                        .where(ytd)
                        .groupBy(mc.menuCategoryId, mc.menuCategoryName)
                        .orderBy(cod.lineTotal.sum().desc())
                        .limit(1)
        ).fetchOne();

        String     topCategoryName  = topCat == null ? null : topCat.get(0, String.class);
        BigDecimal topCategorySales = topCat == null ? BigDecimal.ZERO : nz(topCat.get(1, BigDecimal.class));

        //  - Top3 메뉴
        List<Tuple> topMenusT = readHints(
                query.select(m.menuId, m.menuName, cod.quantity.sum(), cod.lineTotal.sum())
                        .from(co)
                        .join(cod).on(cod.order.eq(co))
                        .join(cod.menuIdFk, m)         // ✅ 연관조인
                        .where(ytd)
                        .groupBy(m.menuId, m.menuName)
                        .orderBy(cod.lineTotal.sum().desc())
                        .limit(3)
        ).fetch();

        List<TopMenuItem> topMenus = new ArrayList<>(topMenusT.size());
        BigDecimal top3Sum = BigDecimal.ZERO;
        for (Tuple r : topMenusT) {
            Long       mid    = r.get(0, Long.class);
            String     mname  = r.get(1, String.class);
            Long       qty    = Optional.ofNullable(r.get(2, Integer.class)).map(Integer::longValue).orElse(0L);
            BigDecimal msales = nz(r.get(3, BigDecimal.class));
            top3Sum = top3Sum.add(msales);
            topMenus.add(TopMenuItem.builder()
                    .menuId(mid).menuName(mname).quantity(qty).sales(msales).build());
        }

        //  - 분모: 라인매출/카테고리수/메뉴수
        Tuple denom = readHints(
                query.select(
                                cod.lineTotal.sum(),
                                mc.menuCategoryId.countDistinct(),
                                m.menuId.countDistinct()
                        )
                        .from(co)
                        .join(cod).on(cod.order.eq(co))
                        .join(cod.menuIdFk, m)
                        .join(m.menuCategory, mc)
                        .where(ytd)
        ).fetchOne();

        BigDecimal totalLineSales = denom != null ? nz(denom.get(0, BigDecimal.class)) : BigDecimal.ZERO;
        long categoryCount        = denom != null ? nz(denom.get(1, Long.class))       : 0L;
        long menuCount            = denom != null ? nz(denom.get(2, Long.class))       : 0L;

        BigDecimal topCategoryRatio = divOrZero(topCategorySales.multiply(BigDecimal.valueOf(100)), totalLineSales, 1);
        BigDecimal menuTop3Ratio    = divOrZero(top3Sum.multiply(BigDecimal.valueOf(100)),         totalLineSales, 1);

        dto.setCategoryCount(categoryCount);
        dto.setCategoryName(topCategoryName);
        dto.setCategoryRatio(topCategoryRatio);
        dto.setMenuCount(menuCount);
        dto.setMenuTop3Ratio(menuTop3Ratio);
        dto.setTopMenus(topMenus);

        return dto;
    }




    /* =========================================================
       주문 목록 (카테고리/메뉴/주문형태 등)
       ========================================================= */
    @Override
    @Transactional(readOnly = true)
    public Page<OrdersRowDto> findOrders(AnalyticsSearchDto cond, Pageable pageable) {
        boolean byMonth = cond.getViewBy() == ViewBy.MONTH;
        String fmt = byMonth ? "%Y-%m" : "%Y-%m-%d";
        StringExpression labelExpr = dateFormat(co.orderedAt, fmt);

        // 공통 필터
        BooleanExpression filter = co.status.eq(OrderStatus.COMPLETED);
        if (cond.getStartDate() != null || cond.getEndDate() != null) {
            filter = filter.and(betweenDateClosedOpen(co.orderedAt, cond.getStartDate(), cond.getEndDate()));
        }
        if (cond.getStoreIds() != null && !cond.getStoreIds().isEmpty()) {
            filter = filter.and(s.id.in(cond.getStoreIds()));
        }

        StringExpression groupKey = Expressions.stringTemplate(
                "CONCAT_WS('|',{0},{1},{2},{3},{4})",
                s.id, mc.menuCategoryId, m.menuId, labelExpr, co.orderType.stringValue()
        );

        Long total = Optional.ofNullable(
                readHints(
                        query.select(Expressions.numberTemplate(Long.class, "COUNT(DISTINCT {0})", groupKey))
                                .from(co)
                                .join(cod).on(cod.order.eq(co))
                                .join(co.storeIdFk, s)
                                .join(cod.menuIdFk, m)
                                .join(m.menuCategory, mc)
                                .where(filter)
                ).fetchOne()
        ).orElse(0L);

        if (total == 0L) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0L);
        }

        List<OrdersRowDto> rows = readHints(
                query.select(Projections.bean(OrdersRowDto.class,
                                labelExpr.as("date"),
                                s.name.as("storeName"),
                                mc.menuCategoryName.as("category"),
                                m.menuName.as("menu"),
                                cod.lineTotal.sum().as("menuSales"),
                                cod.quantity.sum().longValue().as("menuCount"),
                                co.orderType.stringValue().as("orderType"),
                                labelExpr.as("orderDate"),
                                co.id.min().as("orderId"),
                                s.id.as("storeId")
                        ))
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
                                s.id.asc(), m.menuName.asc()
                        )
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
        ).fetch();

        if (rows.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }

        // === 버킷 집합 수집 (storeId, label, orderType)
        Set<Long> sids = new HashSet<>();
        Set<String> labels = new HashSet<>();
        Set<OrderType> types = new HashSet<>();
        for (OrdersRowDto r : rows) {
            sids.add(r.getStoreId());
            labels.add(r.getDate());
            if (r.getOrderType() != null) {
                types.add(OrderType.valueOf(r.getOrderType()));
            }
        }

        // 버킷 집계(co 기반: 주문건수/주문총액) — 한방
        StringExpression labelForAgg = dateFormat(co.orderedAt, fmt);

        List<Tuple> bucketAgg = readHints(
                query.select(
                                s.id,
                                labelForAgg,
                                co.orderType,
                                co.id.countDistinct(),   // 주문 건수
                                co.totalPrice.sum()      // 주문 매출(주문총액)
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
        ).fetch();

        record Key(Long sid, String label, OrderType type) {}
        Map<Key, Tuple> bucketMap = new HashMap<>();
        for (Tuple t : bucketAgg) {
            bucketMap.put(new Key(t.get(0, Long.class), t.get(1, String.class), t.get(2, OrderType.class)), t);
        }

        // DTO에 주문건수/주문매출 값 merge
        for (OrdersRowDto r : rows) {
            OrderType ot = (r.getOrderType() == null) ? null : OrderType.valueOf(r.getOrderType());
            Tuple b = (ot == null) ? null : bucketMap.get(new Key(r.getStoreId(), r.getDate(), ot));
            if (b != null) {
                r.setOrderCount(Optional.ofNullable(b.get(3, Long.class)).orElse(0L));
                r.setOrderSales(Optional.ofNullable(b.get(4, BigDecimal.class)).orElse(BigDecimal.ZERO));
            }
        }

        return new PageImpl<>(rows, pageable, total);
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

        // 1) 점포 현재고 합 (store_inventory)
        BigDecimal storeQtyBd = Optional.ofNullable(
                readHints(query.select(si.quantity.sum())
                        .from(si)
                        .join(si.store, s)
                        .where(storeFilter)
                ).fetchOne()
        ).orElse(BigDecimal.ZERO);
        long storeQty = storeQtyBd.longValue();

        // 2) 발주 수량 합 (기간) — 필드명이 달라도 동작 (quantity 기반)
        BooleanExpression period = betweenDateClosedOpen(ro.actualDeliveryDate, cond.getStartDate(), cond.getEndDate());

        // 만약 rod.quantity 필드명이 다르면 Q클래스 기준으로 바꿔 주세요.
        Integer orderVolI = Optional.ofNullable(
                readHints(query.select(rod.detailCount.sum())
                        .from(rod)
                        .join(rod.receiveOrder, ro)
                        .join(ro.store, s)
                        .where(storeFilter, period)
                ).fetchOne()
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

        List<Tuple> rows = readHints(
                query
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
        ).fetch();

        // (B) 현재고 (해당 (store, material) 조합만)
        Map<String, Long> onhand = new HashMap<>();
        if (!rows.isEmpty()) {
            Set<Long> sids = new HashSet<>(), mids = new HashSet<>();
            for (Tuple t : rows) { sids.add(t.get(1, Long.class)); mids.add(t.get(3, Long.class)); }

            List<Tuple> inv = readHints(query
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
            ).fetch();

            for (Tuple t : inv) {
                String key = t.get(0, Long.class) + ":" + t.get(1, Long.class);
                BigDecimal q = Optional.ofNullable(t.get(2, BigDecimal.class)).orElse(BigDecimal.ZERO);
                onhand.put(key, q.longValue());
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

    /** 읽기 힌트 공통 적용 */
    private <T> JPAQuery<T> readHints(JPAQuery<T> q) {
        return q.setHint("org.hibernate.readOnly", true)
                .setHint("org.hibernate.flushMode", "COMMIT")
                .setHint("jakarta.persistence.query.timeout", 15000);
    }

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
    private BooleanExpression betweenDateClosedOpen(DateTimePath<LocalDateTime> path, LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            return path.goe(start.atStartOfDay()).and(path.lt(end.plusDays(1).atStartOfDay()));
        } else if (start != null) {
            return path.goe(start.atStartOfDay());
        } else if (end != null) {
            return path.lt(end.plusDays(1).atStartOfDay());
        } else return null;
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

    private NumberExpression<BigDecimal> sumIf(BooleanExpression cond, NumberExpression<BigDecimal> val) {
        return new CaseBuilder()
                .when(cond).then(val)
                .otherwise(Expressions.constant(BigDecimal.ZERO))
                .sum();
    }

    /** SUM(CASE WHEN cond THEN 1 ELSE 0 END) */
    private NumberExpression<Long> countIf(BooleanExpression cond) {
        return Expressions.numberTemplate(Long.class, "SUM(CASE WHEN {0} THEN 1 ELSE 0 END)", cond);
    }

    /** 0-나눗셈 보호 */
    private static BigDecimal divOrZero(BigDecimal num, BigDecimal den, int scale) {
        return (den == null || den.signum() == 0) ? BigDecimal.ZERO : num.divide(den, scale, RoundingMode.HALF_UP);
    }

    /** nvl for wrapper types */
    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private static Long       nz(Long v)       { return v == null ? 0L : v; }
    @SuppressWarnings("unused")
    private static Integer    nz(Integer v)    { return v == null ? 0 : v; }


    /**  내부용 구조체  */
    private static class GlobalComp {
        static final GlobalComp ZERO = new GlobalComp(BigDecimal.ZERO, BigDecimal.ZERO);
        final BigDecimal compMoM;
        final BigDecimal compYoY;
        GlobalComp(BigDecimal mom, BigDecimal yoy) { this.compMoM = mom; this.compYoY = yoy; }
    }

    /** 단일 점포 선택 시 (MTD/PMT/YTD/LYTD) — WHERE에 넓은 기간 추가 */
    private GlobalComp computeGlobalComp(AnalyticsSearchDto cond) {
        final var today     = LocalDate.now(ZONE_SEOUL);

        MoMWindows w = calcMoMWindows(today);
        final var aStart = w.aStart();
        final var aEnd   = w.aEnd();
        final var bStart = w.bStart();
        final var bEnd   = w.bEnd();

        final var ytdStart  = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd    = today.minusDays(1);
        final var lytdStart = ytdStart.minusYears(1);
        final var lytdEnd   = ytdEnd.minusYears(1);

        BooleanExpression base =
                co.status.eq(OrderStatus.COMPLETED)
                        .and((cond.getStoreIds()!=null && !cond.getStoreIds().isEmpty()) ? s.id.in(cond.getStoreIds()) : null)
                        .and(betweenDateClosedOpen(co.orderedAt, lytdStart, ytdEnd)); // 스캔 범위

        Tuple t = readHints(
                query.select(
                                // A / B
                                sumIf(betweenDateClosedOpen(co.orderedAt, aStart, aEnd), co.totalPrice),   // 0
                                sumIf(betweenDateClosedOpen(co.orderedAt, bStart, bEnd), co.totalPrice),   // 1
                                // YoY용
                                sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart,  ytdEnd),  co.totalPrice), // 2
                                sumIf(betweenDateClosedOpen(co.orderedAt, lytdStart, lytdEnd), co.totalPrice)  // 3
                        )
                        .from(co).join(co.storeIdFk, s)
                        .where(base)
        ).fetchOne();

        BigDecimal a  = t != null ? nz(t.get(0, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal b  = t != null ? nz(t.get(1, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal y  = t != null ? nz(t.get(2, BigDecimal.class)) : BigDecimal.ZERO;
        BigDecimal ly = t != null ? nz(t.get(3, BigDecimal.class)) : BigDecimal.ZERO;

        BigDecimal mom = (b.signum()==0) ? BigDecimal.ZERO
                : a.divide(b, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
        BigDecimal yoy = (ly.signum()==0) ? BigDecimal.ZERO
                : y.divide(ly, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));

        return new GlobalComp(mom, yoy);
    }

    /** 여러 점포 선택/전사 — WHERE에 넓은 기간 추가 */
    private Map<Long, GlobalComp> computeCompByStore(Set<Long> sidsOnPage) {
        Map<Long, GlobalComp> out = new HashMap<>();
        if (sidsOnPage == null || sidsOnPage.isEmpty()) return out;

        final var today = LocalDate.now(ZONE_SEOUL);

        MoMWindows w = calcMoMWindows(today);
        final var aStart = w.aStart();
        final var aEnd   = w.aEnd();
        final var bStart = w.bStart();
        final var bEnd   = w.bEnd();

        final var ytdStart  = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd    = today.minusDays(1);
        final var lytdStart = ytdStart.minusYears(1);
        final var lytdEnd   = ytdEnd.minusYears(1);

        List<Tuple> rows = readHints(
                query.select(
                                s.id,
                                // A / B
                                sumIf(betweenDateClosedOpen(co.orderedAt, aStart, aEnd), co.totalPrice),
                                sumIf(betweenDateClosedOpen(co.orderedAt, bStart, bEnd), co.totalPrice),
                                // YoY
                                sumIf(betweenDateClosedOpen(co.orderedAt, ytdStart,  ytdEnd),  co.totalPrice),
                                sumIf(betweenDateClosedOpen(co.orderedAt, lytdStart, lytdEnd), co.totalPrice)
                        )
                        .from(co).join(co.storeIdFk, s)
                        .where(
                                co.status.eq(OrderStatus.COMPLETED),
                                s.id.in(sidsOnPage),
                                betweenDateClosedOpen(co.orderedAt, lytdStart, ytdEnd)
                        )
                        .groupBy(s.id)
        ).fetch();

        for (Tuple t : rows) {
            Long sid = t.get(0, Long.class);
            BigDecimal a  = nz(t.get(1, BigDecimal.class));
            BigDecimal b  = nz(t.get(2, BigDecimal.class));
            BigDecimal y  = nz(t.get(3, BigDecimal.class));
            BigDecimal ly = nz(t.get(4, BigDecimal.class));

            BigDecimal mom = (b.signum()==0) ? BigDecimal.ZERO
                    : a.divide(b, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
            BigDecimal yoy = (ly.signum()==0) ? BigDecimal.ZERO
                    : y.divide(ly, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
            out.put(sid, new GlobalComp(mom, yoy));
        }
        return out;
    }


    private record MoMWindows(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {}

    /** MoM 비교 기간 산출
     *  - 매월 1일:   A=전월 전체,        B=전전월 전체
     *  - 그 외 날짜: A=이번달 1~어제,     B=지난달 1~A와 동일 일수(월말 캡)
     */
    private MoMWindows calcMoMWindows(LocalDate today) {
        if (today.getDayOfMonth() == 1) {
            LocalDate prevStart = today.minusMonths(1).withDayOfMonth(1);
            LocalDate prevEnd   = prevStart.plusMonths(1).minusDays(1);
            LocalDate pprevStart = today.minusMonths(2).withDayOfMonth(1);
            LocalDate pprevEnd   = pprevStart.plusMonths(1).minusDays(1);
            return new MoMWindows(prevStart, prevEnd, pprevStart, pprevEnd);
        } else {
            LocalDate aStart = today.withDayOfMonth(1);
            LocalDate aEnd   = today.minusDays(1);
            long dayCount = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(aStart, aEnd) + 1);

            LocalDate bStart = aStart.minusMonths(1);
            LocalDate bEnd   = bStart.plusDays(dayCount - 1);
            LocalDate bLast  = bStart.plusMonths(1).minusDays(1);
            if (bEnd.isAfter(bLast)) bEnd = bLast;

            return new MoMWindows(aStart, aEnd, bStart, bEnd);
        }
    }
}

