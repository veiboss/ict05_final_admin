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
import com.querydsl.core.types.*;
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
                        .orderBy(orderByNull())
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

        // total count (per-store rows만 기준, 페이징 기준은 동일 유지)
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

        // 본문: 매출/거래수 (per-store)
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

        // 같은 필터로 '수량' 합 (store+label)
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
                    Optional.ofNullable(t.get(2, Integer.class)).orElse(0));
        }

        // Comp 계산 소스
        Set<Long> pageSids = new HashSet<>();
        for (Tuple t : baseRows) pageSids.add(t.get(0, Long.class));

        List<Long> selected = cond.getStoreIds();
        boolean isAll   = (selected == null || selected.isEmpty());
        boolean isMulti = (selected != null && selected.size() > 1);

        Map<Long, GlobalComp> perStoreComp = null;
        GlobalComp globalCompForTotal = computeGlobalComp(cond); // Total 행은 항상 "선택 범위 전체" 기준

        if (isAll || isMulti) {
            perStoreComp = computeCompByStore(pageSids);
        }

        // per-store DTO 구성
        List<KpiRowDto> storeContent = new ArrayList<>(baseRows.size());
        for (Tuple t : baseRows) {
            Long sid      = t.get(0, Long.class);
            String sname  = t.get(1, String.class);
            String label  = t.get(2, String.class);
            BigDecimal sales = Optional.ofNullable(t.get(3, BigDecimal.class)).orElse(BigDecimal.ZERO);
            long trx         = Optional.ofNullable(t.get(4, Long.class)).orElse(0L);

            int unitsI = unitsMap.getOrDefault(new Key(sid, label), 0);
            BigDecimal units = BigDecimal.valueOf((long) unitsI);
            BigDecimal trxBd = trx > 0 ? BigDecimal.valueOf(trx) : BigDecimal.ZERO;

            BigDecimal ads = divOrZero(sales, trxBd, 2);
            BigDecimal upt = divOrZero(units, trxBd, 6);
            BigDecimal aur = divOrZero(sales, units, 2);

            GlobalComp compVal = (isAll || isMulti)
                    ? perStoreComp.getOrDefault(sid, GlobalComp.ZERO)
                    : globalCompForTotal; // 단일 선택이면 전체=해당점포

            storeContent.add(KpiRowDto.builder()
                    .date(label)
                    .storeName(sname)
                    .sales(sales)
                    .transaction(trx)
                    .upt(upt)
                    .ads(ads)
                    .aur(aur)
                    .compMoM(compVal.compMoM.setScale(1, RoundingMode.HALF_UP))
                    .compYoY(compVal.compYoY.setScale(1, RoundingMode.HALF_UP))
                    .build());
        }

        // ✅ showTotal=true면 라벨별 Total 행 생성/삽입
        if (Boolean.TRUE.equals(cond.getShowTotal())) {
            // 라벨별 per-store 묶기(라벨 내 매출 내림차순 유지)
            Map<String, List<KpiRowDto>> byLabel = new LinkedHashMap<>();
            storeContent.sort(Comparator
                    .comparing(KpiRowDto::getDate).reversed()
                    .thenComparing((KpiRowDto r) -> r.getSales() == null ? BigDecimal.ZERO : r.getSales(), Comparator.reverseOrder())
            );
            for (KpiRowDto r : storeContent) {
                byLabel.computeIfAbsent(r.getDate(), k -> new ArrayList<>()).add(r);
            }

            // 라벨별 units 합 계산
            Map<String, Long> unitsByLabel = new HashMap<>();
            for (Map.Entry<Key, Integer> e : unitsMap.entrySet()) {
                unitsByLabel.merge(e.getKey().label(), e.getValue().longValue(), Long::sum);
            }

            List<KpiRowDto> out = new ArrayList<>();
            for (Map.Entry<String, List<KpiRowDto>> e : byLabel.entrySet()) {
                String label = e.getKey();
                List<KpiRowDto> rows = e.getValue();

                BigDecimal sumSales = rows.stream().map(x -> x.getSales()==null?BigDecimal.ZERO:x.getSales())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                long sumTrx = rows.stream().mapToLong(x -> x.getTransaction()==null?0L:x.getTransaction()).sum();
                long sumUnits = unitsByLabel.getOrDefault(label, 0L);

                BigDecimal trxBd = sumTrx > 0 ? BigDecimal.valueOf(sumTrx) : BigDecimal.ZERO;
                BigDecimal unitsBd = BigDecimal.valueOf(sumUnits);

                KpiRowDto totalRow = KpiRowDto.builder()
                        .date(label)
                        .storeName("Total")
                        .sales(sumSales)
                        .transaction(sumTrx)
                        .upt(divOrZero(unitsBd, trxBd, 6))
                        .ads(divOrZero(sumSales, trxBd, 2))
                        .aur(divOrZero(sumSales, unitsBd, 2))
                        .compMoM(globalCompForTotal.compMoM.setScale(1, RoundingMode.HALF_UP))
                        .compYoY(globalCompForTotal.compYoY.setScale(1, RoundingMode.HALF_UP))
                        .build();

                out.add(totalRow);     // 맨 위에 Total
                out.addAll(rows);      // 그 다음 매장들
            }
            return new PageImpl<>(out, pageable, total); // total은 기존 per-store 기준 유지
        }

        // Total 숨김이면 per-store만 반환
        return new PageImpl<>(storeContent, pageable, total);
    }


    private KpiRowDto buildKpiGrandTotal(AnalyticsSearchDto cond) {
        // 공통 WHERE (상태 + 기간 + 매장)
        BooleanExpression base = eqKpiFilter(cond, co, s);

        // 매출/거래수
        Tuple t = readHints(
                query.select(co.totalPrice.sum(), co.id.countDistinct())
                        .from(co).join(co.storeIdFk, s)
                        .where(base)
                        .orderBy(orderByNull())
        ).fetchOne();

        BigDecimal sales = (t==null || t.get(0, BigDecimal.class)==null) ? BigDecimal.ZERO : t.get(0, BigDecimal.class);
        long trx        = (t==null || t.get(1, Long.class)==null)       ? 0L               : t.get(1, Long.class);

        // 판매수량(Units)
        Long unitsL = Optional.ofNullable(
                readHints(
                        query.select(cod.quantity.sum().longValue())
                                .from(cod).join(cod.order, co).join(co.storeIdFk, s)
                                .where(base)
                                .orderBy(orderByNull())
                ).fetchOne()
        ).orElse(0L);

        BigDecimal units = BigDecimal.valueOf(unitsL);
        BigDecimal trxBd = trx > 0 ? BigDecimal.valueOf(trx) : BigDecimal.ZERO;

        BigDecimal upt = divOrZero(units, trxBd, 6);
        BigDecimal ads = divOrZero(sales, trxBd, 2);
        BigDecimal aur = divOrZero(sales, units, 2);

        // Comp은 현재 필터 전체 기준으로 계산(전사/다중/단일 동일 규칙)
        GlobalComp comp = computeGlobalComp(cond);

        return KpiRowDto.builder()
                .date(rangeLabel(cond.getStartDate(), cond.getEndDate()))
                .storeName("Total")
                .sales(sales)
                .transaction(trx)
                .upt(upt)
                .ads(ads)
                .aur(aur)
                .compMoM(comp.compMoM.setScale(1, RoundingMode.HALF_UP))
                .compYoY(comp.compYoY.setScale(1, RoundingMode.HALF_UP))
                .build();
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

        // ✅ 1) 트랜잭션 + 채널별 매출 (co 단일 스캔)
        final BooleanExpression ytd = co.status.eq(OrderStatus.COMPLETED)
                .and(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd));

        OrdersCardsDto dto = readHints(
                query.select(Projections.bean(OrdersCardsDto.class,
                                countIf(ytd).as("transaction"),
                                sumIf(ytd.and(co.orderType.eq(OrderType.VISIT)),    co.totalPrice).as("visitSales"),
                                sumIf(ytd.and(co.orderType.eq(OrderType.TAKEOUT)),  co.totalPrice).as("takeoutSales"),
                                sumIf(ytd.and(co.orderType.eq(OrderType.DELIVERY)), co.totalPrice).as("deliverySales")
                        ))
                        .from(co)
                        .where(ytd)
                        .orderBy(orderByNull()) // ✅ filesort 제거
        ).fetchOne();
        if (dto == null) dto = new OrdersCardsDto();

    /* =========================================================
       2) 메뉴 Top3 (co → cod → menu)
       - 드라이빙 테이블을 co로 변경 (EXISTS 제거)
       - cod 커버링 인덱스 사용: (order_id_fk, menu_id_fk, total, qty)
       ========================================================= */
        List<Tuple> topMenusT = readHints(
                query.select(
                                m.menuId,
                                m.menuName,
                                // ✅ 수량 합계 (Integer 타입 일치)
                                cod.quantity.sum()
                                        .coalesce(Expressions.constant(0)), // Integer 상수
                                // ✅ 매출 합계 (BigDecimal 타입 일치)
                                cod.lineTotal.sum()
                                        .coalesce(Expressions.constant(BigDecimal.ZERO)),
                                co.id.min() // Hibernate EXISTS 방지용 더미
                        )
                        .from(co)
                        .join(cod).on(cod.order.eq(co))
                        .join(cod.menuIdFk, m)
                        .where(
                                co.status.eq(OrderStatus.COMPLETED),
                                betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd)
                        )
                        .groupBy(m.menuId, m.menuName)
                        .orderBy(cod.lineTotal.sum().desc())
                        .limit(3)
        ).fetch();

        List<TopMenuItem> topMenus = new ArrayList<>(topMenusT.size());
        for (Tuple r : topMenusT) {
            Integer qtyI = Optional.ofNullable(r.get(2, Integer.class)).orElse(0);
            BigDecimal sales = Optional.ofNullable(r.get(3, BigDecimal.class)).orElse(BigDecimal.ZERO);

            topMenus.add(TopMenuItem.builder()
                    .menuId(r.get(0, Long.class))
                    .menuName(r.get(1, String.class))
                    .quantity(qtyI.longValue()) // ✅ Integer → Long 변환
                    .sales(sales)
                    .build());
        }
        dto.setTopMenus(topMenus);

    /* =========================================================
       3) 카테고리 집계 (co → cod → menu → menu_category)
       - EXISTS 제거
       - co를 드라이빙 테이블로 두고 기간 range scan
       - 커버링 인덱스, filesort 제거
       ========================================================= */
        List<Tuple> catAgg = readHints(
                query
                        .select(
                                mc.menuCategoryId,
                                mc.menuCategoryName,
                                cod.quantity.sum().coalesce(Expressions.constant(0)),
                                cod.lineTotal.sum().coalesce(Expressions.constant(BigDecimal.ZERO)),
                                co.id.min()
                        )
                        // ✅ FROM절에 직접 STRAIGHT_JOIN 삽입 (EntityPath를 템플릿으로 감싸기)
                        .from(co)
                        .setHint("jakarta.persistence.query.hint", "/*+ STRAIGHT_JOIN */")
                        .join(cod).on(cod.order.eq(co))
                        .join(cod.menuIdFk, m)
                        .join(m.menuCategory, mc)
                        .where(
                                co.status.eq(OrderStatus.COMPLETED),
                                betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd)
                        )
                        .groupBy(mc.menuCategoryId, mc.menuCategoryName)
                        .orderBy(orderByNull())
        ).fetch();

        List<CategoryStat> cats = new ArrayList<>(catAgg.size());
        long totalUnits = 0L;
        for (Tuple t : catAgg) {
            Integer unitsI = Optional.ofNullable(t.get(2, Integer.class)).orElse(0);
            BigDecimal sales = Optional.ofNullable(t.get(3, BigDecimal.class)).orElse(BigDecimal.ZERO);

            cats.add(CategoryStat.builder()
                    .categoryId(t.get(0, Long.class))
                    .categoryName(t.get(1, String.class))
                    .units(unitsI.longValue()) // ✅ 동일하게 Integer → Long
                    .sales(sales)
                    .build());
            totalUnits += unitsI;
        }

        List<CategoryStat> categoriesByCount = new ArrayList<>(cats);
        categoriesByCount.sort(Comparator.comparingLong(CategoryStat::getUnits).reversed());

        List<CategoryStat> categoriesBySales = new ArrayList<>(cats);
        categoriesBySales.sort(Comparator.comparing(CategoryStat::getSales).reversed());

        dto.setCategoriesByCount(categoriesByCount);
        dto.setCategoriesBySales(categoriesBySales);
        dto.setMenuCount(totalUnits);

        return dto;
    }




    /* =========================================================
       주문 목록 (카테고리/메뉴/주문형태 등)
       ========================================================= */
    @Override
    @Transactional(readOnly = true)
    public Page<OrdersRowDto> findOrders(AnalyticsSearchDto cond, Pageable pageable) {

        final boolean byMonth = (cond.getViewBy() == ViewBy.MONTH);

        // ✅ 표시(라벨)용 포맷만 남기고, GROUP BY/ORDER BY는 원본 DATE 컬럼 사용
        // - 일별:   label = DATE_FORMAT(customer_order_date, '%Y-%m-%d')
        // - 월별:   label = CONCAT(YEAR(customer_order_date), '-', LPAD(MONTH(customer_order_date),2,'0'))
        StringExpression dayLabel  = Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", co.orderedAt);
        StringExpression monthLabel= Expressions.stringTemplate("CONCAT(YEAR({0}), '-', LPAD(MONTH({0}),2,'0'))", co.orderedAt);

        NumberExpression<Integer> yExpr = Expressions.numberTemplate(Integer.class, "YEAR({0})",  co.orderedAt);
        NumberExpression<Integer> mExpr = Expressions.numberTemplate(Integer.class, "MONTH({0})", co.orderedAt);

        // 공통 WHERE
        BooleanExpression filter = co.status.eq(OrderStatus.COMPLETED);
        if (cond.getStartDate() != null || cond.getEndDate() != null) {
            filter = filter.and(betweenDateClosedOpen(co.orderedAt, cond.getStartDate(), cond.getEndDate()));
        }
        if (cond.getStoreIds() != null && !cond.getStoreIds().isEmpty()) {
            filter = filter.and(s.id.in(cond.getStoreIds()));
        }

        /* -------------------- ① total count -------------------- */
        Long total = Optional.ofNullable(
                readHints(
                        byMonth
                                // 월별: (연,월,점포) 개수
                                ? query.select(Expressions.numberTemplate(Long.class,
                                        "COUNT(DISTINCT CONCAT_WS('|', {0}, {1}, {2}))", s.id, yExpr, mExpr))
                                .from(co).join(co.storeIdFk, s)
                                .where(filter)
                                // 일별: (점포, 카테고리, 메뉴, '원본 DATE', 주문형태)
                                : query.select(Expressions.numberTemplate(Long.class,
                                        "COUNT(DISTINCT {0}, {1}, {2}, {3}, {4})",
                                        s.id, mc.menuCategoryId, m.menuId, co.orderedAt, co.orderType))
                                .from(co)
                                .join(cod).on(cod.order.eq(co))
                                .join(co.storeIdFk, s)
                                .join(cod.menuIdFk, m)
                                .join(m.menuCategory, mc)
                                .where(filter)
                ).fetchOne()
        ).orElse(0L);
        if (total == 0L) return new PageImpl<>(Collections.emptyList(), pageable, 0L);

        /* -------------------- ② 본문 rows -------------------- */
        List<OrdersRowDto> rows;
        if (byMonth) {
            // ✅ 월별: (연,월,점포) 그룹 / 표시는 monthLabel
            rows = readHints(
                    query.select(Projections.bean(OrdersRowDto.class,
                                    ExpressionUtils.as(monthLabel, "date"),
                                    ExpressionUtils.as(monthLabel, "orderDate"),
                                    ExpressionUtils.as(s.name, "storeName"),
                                    // 메뉴 수량/매출 (detail 기준 합계)
                                    ExpressionUtils.as(
                                            Expressions.numberTemplate(Long.class, "COALESCE(SUM({0}),0)", cod.quantity),
                                            "menuCount"
                                    ),
                                    ExpressionUtils.as(
                                            Expressions.numberTemplate(BigDecimal.class, "COALESCE(SUM({0}),0)", cod.lineTotal),
                                            "menuSales"
                                    ),
                                    ExpressionUtils.as(s.id, "storeId")
                            ))
                            .from(cod)
                            .join(cod.order, co)
                            .join(co.storeIdFk, s)
                            .where(filter)
                            .groupBy(s.id, s.name, yExpr, mExpr) // ⬅️ 원본 DATE 파생 컬럼(정수)로 그룹핑
                            .orderBy(yExpr.desc(), mExpr.desc(),  // ⬅️ 정렬도 인덱스 친화적으로
                                    Expressions.numberTemplate(BigDecimal.class, "SUM({0})", cod.lineTotal).desc(),
                                    s.id.asc())
                            .offset(pageable.getOffset())
                            .limit(pageable.getPageSize())
            ).fetch();
        } else {
            // ✅ 일별: (점포×카테고리×메뉴×'원본 DATE'×채널)
            rows = readHints(
                    query.select(Projections.bean(OrdersRowDto.class,
                                    dayLabel.as("date"),                       // 표시는 포맷
                                    s.name.as("storeName"),
                                    mc.menuCategoryName.as("category"),
                                    m.menuName.as("menu"),
                                    Expressions.numberTemplate(BigDecimal.class, "COALESCE(SUM({0}),0)", cod.lineTotal).as("menuSales"),
                                    Expressions.numberTemplate(Long.class,     "COALESCE(SUM({0}),0)", cod.quantity).as("menuCount"),
                                    co.orderType.stringValue().as("orderType"),
                                    dayLabel.as("orderDate"),
                                    co.id.min().as("orderId"),
                                    s.id.as("storeId")
                            ))
                            .from(cod)
                            .join(cod.order, co)
                            .join(co.storeIdFk, s)
                            .join(cod.menuIdFk, m)
                            .join(m.menuCategory, mc)
                            .where(filter)
                            .groupBy(
                                    s.id, s.name,
                                    mc.menuCategoryName, m.menuName,
                                    co.orderedAt,      // ⬅️ 함수 대신 원본 DATE 컬럼
                                    co.orderType
                            )
                            .orderBy(
                                    co.orderedAt.desc(),  // ⬅️ 원본 DATE desc
                                    Expressions.numberTemplate(BigDecimal.class, "SUM({0})", cod.lineTotal).desc(),
                                    s.id.asc(),
                                    m.menuName.asc()
                            )
                            .offset(pageable.getOffset())
                            .limit(pageable.getPageSize())
            ).fetch();
        }
        if (rows.isEmpty()) return new PageImpl<>(Collections.emptyList(), pageable, total);

        /* -------------------- ③ orderCount/orderSales merge -------------------- */
        // 버킷 키도 원본 DATE/연월 기반으로 맞춤
        StringExpression labelKey = byMonth ? monthLabel : dayLabel;

        Set<Long> sids = new HashSet<>();
        for (OrdersRowDto r : rows) sids.add(r.getStoreId());

        if (byMonth) {
            // 월별: (연,월,점포) 버킷
            List<Tuple> bucketAgg = readHints(
                    query.select(
                                    s.id, labelKey,
                                    co.id.countDistinct(),     // orderCount
                                    co.totalPrice.sum()        // orderSales
                            )
                            .from(co).join(co.storeIdFk, s)
                            .where(filter, s.id.in(sids))
                            .groupBy(s.id, yExpr, mExpr)
            ).fetch();

            record MBKey(Long sid, String label) {}
            Map<MBKey, Tuple> map = new HashMap<>();
            for (Tuple t : bucketAgg) map.put(new MBKey(t.get(0, Long.class), t.get(1, String.class)), t);

            for (OrdersRowDto r : rows) {
                Tuple b = map.get(new MBKey(r.getStoreId(), r.getDate()));
                if (b != null) {
                    r.setOrderCount(Optional.ofNullable(b.get(2, Long.class)).orElse(0L));
                    r.setOrderSales(Optional.ofNullable(b.get(3, BigDecimal.class)).orElse(BigDecimal.ZERO));
                }
            }
        } else {
            // 일별: (원본 DATE, 점포, 채널) 버킷
            Set<OrderType> types = new HashSet<>();
            for (OrdersRowDto r : rows) if (r.getOrderType()!=null) types.add(OrderType.valueOf(r.getOrderType()));

            List<Tuple> bucketAgg = readHints(
                    query.select(
                                    s.id, labelKey, co.orderType,
                                    co.id.countDistinct(), co.totalPrice.sum()
                            )
                            .from(co).join(co.storeIdFk, s)
                            .where(
                                    filter,
                                    s.id.in(sids),
                                    !types.isEmpty() ? co.orderType.in(new ArrayList<>(types)) : null
                            )
                            .groupBy(s.id, co.orderedAt, co.orderType) // ⬅️ 원본 DATE
            ).fetch();

            record DBKey(Long sid, String label, OrderType type) {}
            Map<DBKey, Tuple> map = new HashMap<>();
            for (Tuple t : bucketAgg) map.put(new DBKey(t.get(0, Long.class), t.get(1, String.class), t.get(2, OrderType.class)), t);

            for (OrdersRowDto r : rows) {
                OrderType ot = (r.getOrderType()==null) ? null : OrderType.valueOf(r.getOrderType());
                Tuple b = (ot==null)? null : map.get(new DBKey(r.getStoreId(), r.getDate(), ot));
                if (b != null) {
                    r.setOrderCount(Optional.ofNullable(b.get(3, Long.class)).orElse(0L));
                    r.setOrderSales(Optional.ofNullable(b.get(4, BigDecimal.class)).orElse(BigDecimal.ZERO));
                }
            }
        }

        /* -------------------- ④ Total 행 (날짜/월 전체) -------------------- */
        if (Boolean.TRUE.equals(cond.getShowTotal())) {
            Map<String, List<OrdersRowDto>> byLabel = new LinkedHashMap<>();
            for (OrdersRowDto r : rows) byLabel.computeIfAbsent(r.getDate(), k -> new ArrayList<>()).add(r);

            // (A) 메뉴 합계
            Map<String, Tuple> detailTotalsByLabel = new HashMap<>();
            for (Tuple t : readHints(
                    query.select(labelKey, cod.quantity.sum(), cod.lineTotal.sum())
                            .from(cod)
                            .join(cod.order, co)
                            .join(co.storeIdFk, s)
                            .where(filter)
                            .groupBy(byMonth ? new Expression<?>[]{ yExpr, mExpr } : new Expression<?>[]{ co.orderedAt })
                            .orderBy(orderByNull())
            ).fetch()) {
                detailTotalsByLabel.put(t.get(0, String.class), t);
            }

            // (B) 주문 합계
            Map<String, Tuple> orderTotalsByLabel = new HashMap<>();
            for (Tuple t : readHints(
                    query.select(labelKey, co.id.countDistinct(), co.totalPrice.sum())
                            .from(co).join(co.storeIdFk, s)
                            .where(filter)
                            .groupBy(byMonth ? new Expression<?>[]{ yExpr, mExpr } : new Expression<?>[]{ co.orderedAt })
                            .orderBy(orderByNull())
            ).fetch()) {
                orderTotalsByLabel.put(t.get(0, String.class), t);
            }

            List<OrdersRowDto> out = new ArrayList<>(rows.size() + byLabel.size());
            for (Map.Entry<String, List<OrdersRowDto>> e : byLabel.entrySet()) {
                String label = e.getKey();
                Tuple d = detailTotalsByLabel.get(label);
                Tuple o = orderTotalsByLabel.get(label);

                long       menuCount  = d==null ? 0L : Optional.ofNullable(d.get(1, Integer.class)).map(Integer::longValue).orElse(0L);
                BigDecimal menuSales  = d==null ? BigDecimal.ZERO : Optional.ofNullable(d.get(2, BigDecimal.class)).orElse(BigDecimal.ZERO);
                long       orderCount = o==null ? 0L : Optional.ofNullable(o.get(1, Long.class)).orElse(0L);
                BigDecimal orderSales = o==null ? BigDecimal.ZERO : Optional.ofNullable(o.get(2, BigDecimal.class)).orElse(BigDecimal.ZERO);

                OrdersRowDto totalRow = OrdersRowDto.builder()
                        .date(label)
                        .orderDate(byMonth ? "-" : label)
                        .storeName("Total")
                        .orderId(null)
                        .category("-")
                        .menu("-")
                        .menuCount(menuCount)
                        .menuSales(menuSales)
                        .orderCount(orderCount)
                        .orderSales(orderSales)
                        .orderType("-")
                        .storeId(0L)
                        .build();

                out.add(totalRow);
                List<OrdersRowDto> list = e.getValue();
                list.sort(Comparator.comparing(OrdersRowDto::getStoreName, Comparator.nullsLast(String::compareTo)));
                out.addAll(list);
            }
            return new PageImpl<>(out, pageable, total);
        }

        return new PageImpl<>(rows, pageable, total);
    }




    private OrdersRowDto buildOrdersGrandTotal(AnalyticsSearchDto cond) {
        // 기간/매장 WHERE
        BooleanExpression period = betweenDateClosedOpen(co.orderedAt, cond.getStartDate(), cond.getEndDate());
        BooleanExpression storeF = (cond.getStoreIds()!=null && !cond.getStoreIds().isEmpty()) ? s.id.in(cond.getStoreIds()) : null;

        // 주문건수/주문매출 (customer_order)
        Tuple o = readHints(
                query.select(co.id.countDistinct(), co.totalPrice.sum())
                        .from(co).join(co.storeIdFk, s)
                        .where(co.status.eq(OrderStatus.COMPLETED), period, storeF)
                        .orderBy(orderByNull())
        ).fetchOne();
        long orderCnt = (o==null || o.get(0, Long.class)==null) ? 0L : o.get(0, Long.class);
        BigDecimal orderAmt = (o==null || o.get(1, BigDecimal.class)==null) ? BigDecimal.ZERO : o.get(1, BigDecimal.class);

        // 메뉴수량/메뉴매출 (order detail)
        Tuple m = readHints(
                query.select(cod.quantity.sum(), cod.lineTotal.sum())
                        .from(cod).join(cod.order, co).join(co.storeIdFk, s)
                        .where(co.status.eq(OrderStatus.COMPLETED), period, storeF)
                        .orderBy(orderByNull())
        ).fetchOne();
        long menuCnt = Optional.ofNullable(m==null ? null : m.get(0, Integer.class)).map(Integer::longValue).orElse(0L);
        BigDecimal menuAmt = (m==null || m.get(1, BigDecimal.class)==null) ? BigDecimal.ZERO : m.get(1, BigDecimal.class);

        return OrdersRowDto.builder()
                .date(rangeLabel(cond.getStartDate(), cond.getEndDate()))
                .storeName("Total")
                .category(null).menu(null)
                .menuSales(menuAmt)
                .menuCount(menuCnt)
                .orderCount(orderCnt)
                .orderSales(orderAmt)
                .orderType("ALL")
                .orderDate(null)
                .orderId(null)
                .storeId(null)
                .build();
    }

    /* =========================================================
       재료 요약 카드 / 목록
       - HQ 재고(office)는 별도 테이블이 없다면 0으로 두고 훅만 제공
       - 현재고는 store_inventory 합계, 발주는 receive_order_detail 합계
       ========================================================= */
    @Override
    @Transactional(readOnly = true)
    public MaterialsCardsDto findMaterialsSummary() {
        final var today    = LocalDate.now(ZONE_SEOUL);
        final var ytdStart = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd   = today.minusDays(1);

        // (1) HQ 현재고: 아직 테이블 미연결 → 0
        long officeQty = 0L;

        // (2) 가맹점 현재고(전사): store_inventory.quantity 합계
        BigDecimal storeQtyBd = Optional.ofNullable(
                readHints(query.select(si.quantity.sum()).from(si).orderBy(orderByNull()))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);
        long storeQty = storeQtyBd.longValue();

        // (3) YTD 발주 수량(전사)
        Integer orderVolI = Optional.ofNullable(
                readHints(
                        query.select(rod.detailCount.sum())
                                .from(rod)
                                .join(rod.receiveOrder, ro)
                                .where(betweenDateClosedOpen(ro.actualDeliveryDate, ytdStart, ytdEnd))
                                .orderBy(orderByNull())
                ).fetchOne()
        ).orElse(0);

        return MaterialsCardsDto.builder()
                .currentOfficeInventoryQty(officeQty)          // 현재
                .currentTotalStoreInventoryQty(storeQty)       // 현재(전사)
                .orderVolumeQty(orderVolI.longValue())         // YTD(전사)
                .usedQty(0L)                   // Phase A
                .turnoverRate(BigDecimal.ZERO) // Phase A
                .salesAmount(BigDecimal.ZERO)  // Phase A
                .profitAmount(BigDecimal.ZERO) // Phase A
                .avgMargin(BigDecimal.ZERO)    // Phase A
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaterialsRowDto> findMaterials(AnalyticsSearchDto cond, Pageable pageable) {

        final boolean byMonth = cond.getViewBy()==ViewBy.MONTH;
        final String fmt = byMonth ? "%Y-%m" : "%Y-%m-%d";
        final StringExpression labelExpr = dateFormat(ro.actualDeliveryDate, fmt);

        BooleanExpression storeFilter = (cond.getStoreIds()!=null && !cond.getStoreIds().isEmpty())
                ? s.id.in(cond.getStoreIds()) : null;
        BooleanExpression period = betweenDateClosedOpen(ro.actualDeliveryDate, cond.getStartDate(), cond.getEndDate());

        // ----- count (그룹 키) -----
        StringExpression cntKey = byMonth
                ? Expressions.stringTemplate("CONCAT_WS('|',{0},{1},{2})", labelExpr, s.id, mat.id)
                : Expressions.stringTemplate("CONCAT_WS('|',{0},{1},{2},{3})", labelExpr, s.id, mat.id, ro.id);

        Long total = Optional.ofNullable(
                readHints(query.select(Expressions.numberTemplate(Long.class, "COUNT(DISTINCT {0})", cntKey))
                        .from(rod)
                        .join(rod.receiveOrder, ro)
                        .join(ro.store, s)
                        .join(rod.material, mat)
                        .where(storeFilter, period))
                        .fetchOne()
        ).orElse(0L);
        if (total==0L) return new PageImpl<>(Collections.emptyList(), pageable, 0L);

        // ----- 본문 (기존 그대로) -----
        StringExpression poDateExpr = dateFormat(ro.actualDeliveryDate, "%Y-%m-%d");
        NumberExpression<Long> qtySum = Expressions.numberTemplate(Long.class, "COALESCE(SUM({0}),0)", rod.detailCount);

        JPAQuery<Tuple> baseQ = query.select(
                        labelExpr,               // 0
                        s.id, s.name,            // 1,2
                        mat.id, mat.name,        // 3,4
                        byMonth ? Expressions.nullExpression() : ro.id,          // 5
                        byMonth ? Expressions.nullExpression() : poDateExpr,      // 6
                        byMonth ? Expressions.nullExpression() : qtySum,          // 7
                        byMonth ? qtySum : Expressions.nullExpression()           // 8
                )
                .from(rod)
                .join(rod.receiveOrder, ro)
                .join(ro.store, s)
                .join(rod.material, mat)
                .where(storeFilter, period);

        if (byMonth) {
            baseQ.groupBy(labelExpr, s.id, s.name, mat.id, mat.name)
                    .orderBy(labelExpr.desc(), qtySum.desc());
        } else {
            baseQ.groupBy(labelExpr, s.id, s.name, mat.id, mat.name, ro.id, poDateExpr)
                    .orderBy(labelExpr.desc(), poDateExpr.desc(), ro.id.desc());
        }

        List<Tuple> base = readHints(
                baseQ.offset(pageable.getOffset()).limit(pageable.getPageSize())
        ).fetch();

        if (base.isEmpty()) return new PageImpl<>(Collections.emptyList(), pageable, total);

        // ----- 현재고 (페이지에 나온 (sid, mid)만) -----
        Set<Long> sids = new HashSet<>(), mids = new HashSet<>();
        for (Tuple t: base) { sids.add(t.get(1, Long.class)); mids.add(t.get(3, Long.class)); }

        Map<String, Long> onhand = new HashMap<>();
        if (!sids.isEmpty()) {
            List<Tuple> inv = readHints(
                    query.select(s.id, mat.id, si.quantity.sum())
                            .from(si)
                            .join(si.store, s)
                            .join(si.storeMaterial, sm)
                            .join(sm.material, mat)
                            .where(s.id.in(sids), mat.id.in(mids), storeFilter)
                            .groupBy(s.id, mat.id)
            ).fetch();
            for (Tuple t: inv) {
                String k = t.get(0, Long.class)+":"+t.get(1, Long.class);
                onhand.put(k, Optional.ofNullable(t.get(2, BigDecimal.class)).orElse(BigDecimal.ZERO).longValue());
            }
        }

        long dayCount = (cond.getStartDate()!=null && cond.getEndDate()!=null)
                ? Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(cond.getStartDate(), cond.getEndDate())+1) : 0;

        List<MaterialsRowDto> rows = new ArrayList<>(base.size());
        for (Tuple t: base) {
            String label   = t.get(0, String.class);
            Long   sid     = t.get(1, Long.class);
            String sname   = t.get(2, String.class);
            Long   mid     = t.get(3, Long.class);
            String mname   = t.get(4, String.class);

            Long    poId     = byMonth ? null : t.get(5, Long.class);
            String  poDate   = byMonth ? null : t.get(6, String.class);
            Long    poQty    = byMonth ? null : Optional.ofNullable(t.get(7, Long.class)).orElse(0L);
            Long    monthQty = byMonth ? Optional.ofNullable(t.get(8, Long.class)).orElse(0L) : null;

            long onhandQty = onhand.getOrDefault(sid+":"+mid, 0L);

            BigDecimal avgUsage = byMonth
                    ? (dayCount>0 ? new BigDecimal(monthQty).divide(new BigDecimal(dayCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                    : new BigDecimal(Optional.ofNullable(poQty).orElse(0L));

            rows.add(MaterialsRowDto.builder()
                    .orderDate(label)
                    .store(sname)
                    .material(mname)
                    .storeInventoryQty(onhandQty)
                    .purchaseOrderId(poId)
                    .purchaseOrderDate(poDate)
                    .purchaseOrderQty(byMonth ? monthQty : poQty)   // 테이블 헤더가 Amount로 되어 있어도 Qty 합계 사용
                    .turnoverRate(null)          // per-row는 계산 복잡 → 표시는 '-' 처리 권장
                    .profit(null)                // per-row는 표시는 '-' 처리 권장
                    .margin(null)                // per-row는 표시는 '-' 처리 권장
                    .avgUsage(avgUsage)
                    .storeId(sid)
                    .materialId(mid)
                    .build());
        }

        /* ===== 여기서부터 “Total” 행 추가 (라벨별 전 매장 합계) ===== */

        if (Boolean.TRUE.equals(cond.getShowTotal())) {
            // 1) 라벨별 그룹
            Map<String, List<MaterialsRowDto>> byLabel = new LinkedHashMap<>();
            for (MaterialsRowDto r : rows) {
                byLabel.computeIfAbsent(r.getOrderDate(), k -> new ArrayList<>()).add(r);
            }

            // 2) 라벨별 집계 (Qty / Cost / Selling)
            NumberExpression<BigDecimal> costExpr = Expressions.numberTemplate(
                    BigDecimal.class, "SUM({0} * {1})", rod.detailCount, rod.detailUnitPrice);
            NumberExpression<BigDecimal> sellExpr = Expressions.numberTemplate(
                    BigDecimal.class, "SUM({0} * COALESCE({1},0))", rod.detailCount, sm.sellingPrice);

            // (A) 발주 수량 합계 (qty)
            Map<String, Long> qtyByLabel = new HashMap<>();
            for (Tuple t : readHints(
                    query.select(labelExpr, rod.detailCount.sum())
                            .from(rod)
                            .join(rod.receiveOrder, ro)
                            .join(ro.store, s)
                            .where(storeFilter, period)
                            .groupBy(labelExpr)
                            .orderBy(orderByNull())
            ).fetch()) {
                String label = t.get(0, String.class);
                Long qty = Optional.ofNullable(t.get(1, Integer.class)).map(Integer::longValue).orElse(0L);
                qtyByLabel.put(label, qty);
            }

            // (B) 원가/판매가/이익/마진
            Map<String, Tuple> moneyByLabel = new HashMap<>();
            for (Tuple t : readHints(
                    query.select(labelExpr, costExpr, sellExpr)
                            .from(rod)
                            .join(rod.receiveOrder, ro)
                            .join(ro.store, s)
                            .join(rod.material, mat)
                            .leftJoin(sm).on(sm.store.eq(s).and(sm.material.eq(mat)))
                            .where(storeFilter, period)
                            .groupBy(labelExpr)
                            .orderBy(orderByNull())
            ).fetch()) {
                moneyByLabel.put(t.get(0, String.class), t);
            }

            // 3) 출력: Total 먼저, 그 다음 매장행(가독성 위해 Store ASC)
            List<MaterialsRowDto> out = new ArrayList<>(rows.size() + byLabel.size());
            for (Map.Entry<String, List<MaterialsRowDto>> e : byLabel.entrySet()) {
                String label = e.getKey();

                long qty = qtyByLabel.getOrDefault(label, 0L);
                Tuple mny = moneyByLabel.get(label);
                BigDecimal cost  = (mny==null || mny.get(1, BigDecimal.class)==null) ? BigDecimal.ZERO : mny.get(1, BigDecimal.class);
                BigDecimal sales = (mny==null || mny.get(2, BigDecimal.class)==null) ? BigDecimal.ZERO : mny.get(2, BigDecimal.class);
                BigDecimal profit = sales.subtract(cost);
                Long profitLong = (profit == null) ? null : profit.setScale(0, RoundingMode.HALF_UP).longValue();
                BigDecimal margin = (sales.signum()==0) ? null
                        : profit.multiply(BigDecimal.valueOf(100)).divide(sales, 2, RoundingMode.HALF_UP);

                BigDecimal avgUsage = byMonth
                        ? (dayCount>0 ? new BigDecimal(qty).divide(new BigDecimal(dayCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                        : new BigDecimal(qty);

                MaterialsRowDto totalRow = MaterialsRowDto.builder()
                        .orderDate(label)
                        .store("Total")
                        .material("-")
                        .storeInventoryQty(null)      // ⚠️ 재고는 시점 값이어서 날짜 Total에 넣지 않음(오해 방지)
                        .purchaseOrderId(null)
                        .purchaseOrderDate(null)
                        .purchaseOrderQty(qty)        // 테이블의 PurchaseOrderAmount 자리에 바인딩
                        .turnoverRate(null)           // 날짜 단위 계산 어려움 → '-' 표시 권장
                        .profit(profitLong)
                        .margin(margin)
                        .avgUsage(avgUsage)
                        .storeId(0L)
                        .materialId(null)
                        .build();

                out.add(totalRow);

                List<MaterialsRowDto> g = e.getValue();
                g.sort(Comparator.comparing(MaterialsRowDto::getStore, Comparator.nullsLast(String::compareTo)));
                out.addAll(g);
            }
            return new PageImpl<>(out, pageable, total);
        }

        return new PageImpl<>(rows, pageable, total);
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
    public Page<TimeRowDto> findTimeSlices(AnalyticsSearchDto cond, Pageable pageable) {
        return null;
    }


    /* ===================== Helper Methods ===================== */

    /** ORDER BY NULL — filesort 제거용 (카드/단일 집계 전용) */
    private OrderSpecifier<Integer> orderByNull() {
        return new OrderSpecifier<>(Order.ASC, Expressions.nullExpression());
    }


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

    private String rangeLabel(LocalDate start, LocalDate end) {
        if (start == null && end == null) return "";
        if (start == null) return "~ " + end.toString();
        if (end == null) return start.toString() + " ~";
        return start.toString() + " ~ " + end.toString();
    }

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

