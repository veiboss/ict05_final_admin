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
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * AnalyticsRepositoryImpl (지침 반영본)
 * - 기간 조건: [start 00:00, end+1 00:00) 고정
 * - OR 범위 금지 → 한 번의 넓은 기간 WHERE + CASE 집계
 * - 애플리케이션에서만 파생지표 계산(UPT/ADS/AUR/Comp)
 * - 모든 SELECT에 readOnly/timeout 힌트 적용
 */
@Slf4j
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
                                .from(co)
                                .join(cod).on(cod.order.eq(co))
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
        Long total = countKpi(cond);

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


    /* =========================================================
       주문 요약 카드 (YTD) : 상세 로우 가져오지 않음, DB 집계/TopN만 수신
       ========================================================= */
    @Override
    @Transactional(readOnly = true)
    public OrdersCardsDto findOrdersSummary() {
        final var today    = LocalDate.now(ZONE_SEOUL);
        final var ytdStart = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd   = today.minusDays(1);

        NumberExpression<Long> SUM_QTY = Expressions.numberTemplate(
                Long.class, "COALESCE(SUM({0}),0)", cod.quantity);
        NumberExpression<BigDecimal> SUM_AMT = Expressions.numberTemplate(
                BigDecimal.class, "COALESCE(SUM({0}),0)", cod.lineTotal);

        // ✅ 1) 트랜잭션 + 채널별 매출 (co 단일 스캔)
        final BooleanExpression ytd = co.status.eq(OrderStatus.COMPLETED)
                .and(betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd));

        OrdersCardsDto dto = timed("1.병목찾기", () ->
                readHints(
                        query.select(Projections.bean(OrdersCardsDto.class,
                                        co.id.count().as("transaction"), // ✔ where(ytd) 이미 적용됨
                                        sumIf(co.orderType.eq(OrderType.VISIT),    co.totalPrice).as("visitSales"),
                                        sumIf(co.orderType.eq(OrderType.TAKEOUT),  co.totalPrice).as("takeoutSales"),
                                        sumIf(co.orderType.eq(OrderType.DELIVERY), co.totalPrice).as("deliverySales")
                                ))
                                .from(co)
                                .where(ytd)               // ✔ 기간/상태는 여기서만
                                .orderBy(orderByNull())
                ).fetchOne()
        );
        if (dto == null) dto = new OrdersCardsDto();

    /* =========================================================
       2) 메뉴 Top3 (co → cod → menu)
       - 드라이빙 테이블: co
       - cod 커버링 인덱스: (order_id_fk, menu_id_fk, quantity, line_total)
       ========================================================= */
        List<Tuple> topMenusT = timed("2.병목찾기", () -> readHints(
                query.select(
                                m.menuId,
                                m.menuName,
                                SUM_QTY,
                                SUM_AMT
                        )
                        .from(co)
                        .join(cod).on(cod.order.eq(co))     // FK(order_id) 사용
                        .join(cod.menuIdFk, m)
                        .where(
                                co.status.eq(OrderStatus.COMPLETED),
                                betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd)
                        )
                        .groupBy(m.menuId, m.menuName)
                        .orderBy(SUM_AMT.desc())
                        .limit(3)
        ).fetch());

        List<TopMenuItem> topMenus = new ArrayList<>(topMenusT.size());
        for (Tuple r : topMenusT) {
            topMenus.add(TopMenuItem.builder()
                    .menuId(r.get(0, Long.class))
                    .menuName(r.get(1, String.class))
                    .quantity(Optional.ofNullable(r.get(2, Long.class)).orElse(0L))
                    .sales(Optional.ofNullable(r.get(3, BigDecimal.class)).orElse(BigDecimal.ZERO))
                    .build());
        }
        dto.setTopMenus(topMenus);

    /* =========================================================
       3) 카테고리 집계 — 병목 제거 버전
       3a. 대용량 단계: co→cod→menu까지만 조인, "카테고리 ID"로만 집계
       3b. 소수의 ID에 대해 menu_category 이름 조회
       3c. 매핑해서 DTO 구성
       ========================================================= */

        // 3a) id-only 집계 (mc 조인 제거)
        List<Tuple> catAggCore = timed("3a.cat agg (id-only)", () -> readHints(
                query.select(
                                m.menuCategory.menuCategoryId,  // ✅ category_id만
                                SUM_QTY,
                                SUM_AMT
                        )
                        .from(co)
                        .join(cod).on(cod.order.eq(co))        // co → cod
                        .join(cod.menuIdFk, m)                 // → menu
                        .where(
                                co.status.eq(OrderStatus.COMPLETED),
                                betweenDateClosedOpen(co.orderedAt, ytdStart, ytdEnd)
                        )
                        .groupBy(m.menuCategory.menuCategoryId)
                        .orderBy(orderByNull())
        ).fetch());

        // 3b) 이름 붙이기 (가벼움)
        var catIds = catAggCore.stream()
                .map(t -> t.get(0, Long.class))
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        Map<Long, String> catNameMap = catIds.isEmpty() ? Map.of() : timed("3b.load mc names", () -> {
            List<Tuple> names = readHints(
                    query.select(mc.menuCategoryId, mc.menuCategoryName)
                            .from(mc)
                            .where(mc.menuCategoryId.in(catIds))
                            .orderBy(orderByNull())
            ).fetch();
            Map<Long, String> map = new HashMap<>(names.size() * 2);
            for (Tuple t : names) map.put(t.get(0, Long.class), t.get(1, String.class));
            return map;
        });

        // 3c) 매핑
        List<CategoryStat> cats = new ArrayList<>(catAggCore.size());
        long totalUnits = 0L;
        for (Tuple t : catAggCore) {
            Long catId = t.get(0, Long.class);
            long units = Optional.ofNullable(t.get(1, Long.class)).orElse(0L);                // SUM_QTY
            BigDecimal sales = Optional.ofNullable(t.get(2, BigDecimal.class)).orElse(BigDecimal.ZERO); // SUM_AMT
            cats.add(CategoryStat.builder()
                    .categoryId(catId)
                    .categoryName(catNameMap.getOrDefault(catId, "-"))
                    .units(units)
                    .sales(sales)
                    .build());
            totalUnits += units;
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

        // (옵션) 기간이 길면 자동으로 월별로 스위치 (31일 초과 시)
        boolean byMonth = (cond.getViewBy() == ViewBy.MONTH);
        if (cond.getViewBy() == null) {
            byMonth = false; // 기본값: 일별
        }

        StringExpression dayLabel   = dateFormat(co.orderedAt, "%Y-%m-%d");
        StringExpression monthLabel = dateFormat(co.orderedAt, "%Y-%m");
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
        Long total = countOrders(cond);

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
                            .orderBy(
                                    yExpr.desc(),
                                    mExpr.desc()
                                    // ⚠️ 성능상 권장: 집계값 정렬 제거
                                    // Expressions.numberTemplate(BigDecimal.class, "SUM({0})", cod.lineTotal).desc(),
                                    // s.id.asc()
                            )
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
                                    co.orderedAt.desc()  // ⬅️ 원본 DATE desc
                                    // ⚠️ 성능상 권장: 집계값 정렬 제거
                                    // , Expressions.numberTemplate(BigDecimal.class, "SUM({0})", cod.lineTotal).desc()
                                    , s.id.asc()
                                    , m.menuName.asc()
                            )
                            .offset(pageable.getOffset())
                            .limit(pageable.getPageSize())
            ).fetch();
        }
        if (rows.isEmpty()) return new PageImpl<>(Collections.emptyList(), pageable, total);

        /* -------------------- ⚡ 페이지 라벨 윈도우 산출 (③·④ 공용) -------------------- */
        StringExpression labelKey = byMonth ? monthLabel : dayLabel; // 버킷 키도 원본 DATE/연월 기반으로 맞춤

        java.util.Set<String> pageLabels = new java.util.LinkedHashSet<>();
        for (OrdersRowDto r : rows) pageLabels.add(r.getDate());

        BooleanExpression pageWindow = null;
        if (!pageLabels.isEmpty()) {
            if (byMonth) {
                java.time.YearMonth minYM = null, maxYM = null;
                for (String lbl : pageLabels) {
                    java.time.YearMonth ym = java.time.YearMonth.parse(lbl); // "yyyy-MM"
                    if (minYM == null || ym.isBefore(minYM)) minYM = ym;
                    if (maxYM == null || ym.isAfter(maxYM))  maxYM = ym;
                }
                if (minYM != null && maxYM != null) {
                    java.time.LocalDate start = minYM.atDay(1);
                    java.time.LocalDate endPlus1 = maxYM.plusMonths(1).atDay(1); // [start, end+1)
                    pageWindow = co.orderedAt.goe(start.atStartOfDay())
                            .and(co.orderedAt.lt(endPlus1.atStartOfDay()));
                }
            } else {
                java.time.LocalDate minD = null, maxD = null;
                for (String lbl : pageLabels) {
                    java.time.LocalDate d = java.time.LocalDate.parse(lbl); // "yyyy-MM-dd"
                    if (minD == null || d.isBefore(minD)) minD = d;
                    if (maxD == null || d.isAfter(maxD))  maxD = d;
                }
                if (minD != null && maxD != null) {
                    pageWindow = co.orderedAt.goe(minD.atStartOfDay())
                            .and(co.orderedAt.lt(maxD.plusDays(1).atStartOfDay())); // [min, max+1)
                }
            }
        }

        /* -------------------- ③ orderCount/orderSales merge -------------------- */
        java.util.Set<Long> sids = new java.util.HashSet<>();
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
                            .where(filter, s.id.in(sids), pageWindow)   // ✅ 현재 페이지 라벨 윈도우만
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
            java.util.Set<OrderType> types = new java.util.HashSet<>();
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
                                    pageWindow,                                              // ✅ 현재 페이지 라벨 윈도우만
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

            // (A) 메뉴 합계 — 현재 페이지 라벨 윈도우만
            Map<String, Tuple> detailTotalsByLabel = new HashMap<>();
            for (Tuple t : readHints(
                    query.select(labelKey, cod.quantity.sum(), cod.lineTotal.sum())
                            .from(cod)
                            .join(cod.order, co)
                            .join(co.storeIdFk, s)
                            .where(filter, pageWindow) // ✅ 추가
                            .groupBy(byMonth ? new Expression<?>[]{ yExpr, mExpr } : new Expression<?>[]{ co.orderedAt })
                            .orderBy(orderByNull())
            ).fetch()) {
                detailTotalsByLabel.put(t.get(0, String.class), t);
            }

            // (B) 주문 합계 — 현재 페이지 라벨 윈도우만
            Map<String, Tuple> orderTotalsByLabel = new HashMap<>();
            for (Tuple t : readHints(
                    query.select(labelKey, co.id.countDistinct(), co.totalPrice.sum())
                            .from(co).join(co.storeIdFk, s)
                            .where(filter, pageWindow) // ✅ 추가
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


    /* =========================================================
       재료 요약 카드 / 목록
       - 추후 진행
       ========================================================= */
    @Override
    @Transactional(readOnly = true)
    public MaterialsCardsDto findMaterialsSummary() {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaterialsRowDto> findMaterials(AnalyticsSearchDto cond, Pageable pageable) {

        return null;
    }

    /* ===================== 시간·요일 — 차트(YTD) ===================== */
    @Override
    @Transactional(readOnly = true)
    public TimeChartCardDto findTimeChartSummary() {
        final var today    = LocalDate.now(ZONE_SEOUL);
        final var ytdStart = LocalDate.of(today.getYear(), 1, 1);
        final var ytdEnd   = today.minusDays(1);
        return buildTimeChart(null, ytdStart, ytdEnd, true);
    }

    /* ===================== 시간·요일 — 차트(필터적용) ===================== */
    @Override
    @Transactional(readOnly = true)
    public TimeChartRowDto findTimeChart(AnalyticsSearchDto cond) {
        return buildTimeChart(cond.getStoreIds(), cond.getStartDate(), cond.getEndDate(), false);
    }

    /* ===================== 시간·요일 — 표(상세) ===================== */
    @Override
    @Transactional(readOnly = true)
    public Page<TimeRowDto> findTimeRows(AnalyticsSearchDto cond, Pageable pageable) {

        // 공통 WHERE
        BooleanExpression filter = co.status.eq(OrderStatus.COMPLETED)
                .and(betweenDateClosedOpen(co.orderedAt, cond.getStartDate(), cond.getEndDate()));
        if (cond.getStoreIds() != null && !cond.getStoreIds().isEmpty()) {
            filter = filter.and(s.id.in(cond.getStoreIds()));
        }

        final boolean byMonth = (cond.getViewBy() == ViewBy.MONTH);

        // ── 라벨/파생식
        StringExpression dayLabel   = Expressions.stringTemplate("DATE_FORMAT({0}, {1})", co.orderedAt, Expressions.constant("%Y-%m-%d"));
        StringExpression monthLabel = Expressions.stringTemplate("DATE_FORMAT({0}, {1})", co.orderedAt, Expressions.constant("%Y-%m"));

        StringExpression hourSlotExpr = Expressions.stringTemplate(
                "CONCAT(DATE_FORMAT({0}, '%H'), ':00-', DATE_FORMAT({0}, '%H'), ':59')", co.orderedAt);
        StringExpression dayNameExpr = Expressions.stringTemplate(
                "CONCAT('', ELT(DAYOFWEEK({0}), '일','월','화','수','목','금','토'))", co.orderedAt);
        StringExpression orderDateStr = Expressions.stringTemplate(
                "DATE_FORMAT({0}, {1})", co.orderedAt, Expressions.constant("%Y-%m-%d %H:%i"));

        // 집계/정렬용 정수
        NumberExpression<Integer> Y = Expressions.numberTemplate(Integer.class, "YEAR({0})",  co.orderedAt);
        NumberExpression<Integer> M = Expressions.numberTemplate(Integer.class, "MONTH({0})", co.orderedAt);
        NumberExpression<Integer> H = Expressions.numberTemplate(Integer.class, "HOUR({0})",  co.orderedAt);
        NumberExpression<Integer> D = Expressions.numberTemplate(Integer.class, "DAYOFWEEK({0})", co.orderedAt);

        // ① total count
        Long total = countTime(cond);
        if (total == 0L) return new PageImpl<>(Collections.emptyList(), pageable, 0L);

        // ② rows
        List<TimeRowDto> rows;
        if (byMonth) {
            // ✅ 월별 (주문단위)
            NumberExpression<BigDecimal> SUM_AMT =
                    Expressions.numberTemplate(BigDecimal.class, "COALESCE(SUM({0}),0)", co.totalPrice);

            rows = readHints(
                    query.select(Projections.fields(TimeRowDto.class,
                                    monthLabel.as("date"),
                                    s.name.as("storeName"),
                                    ExpressionUtils.as(hourSlotExpr, "hourSlot"),
                                    ExpressionUtils.as(dayNameExpr, "dayOfWeek"),
                                    ExpressionUtils.as(SUM_AMT, "orderAmount"),
                                    co.orderType.stringValue().as("orderType")
                            ))
                            .from(co)
                            .join(co.storeIdFk, s)
                            .where(filter, H.between(8, 22))
                            .groupBy(s.id, s.name, Y, M, H, D, co.orderType)
                            .orderBy(
                                    Y.desc(),   // 최근 월 우선
                                    M.desc(),
                                    H.desc(),   // 같은 월 안에서 최근 시간대 우선
                                    D.desc(),   // (tie-break)
                                    s.name.asc(),
                                    co.orderType.asc()
                            )
                            .offset(pageable.getOffset())
                            .limit(pageable.getPageSize())
            ).fetch();

        } else {
            // ✅ 일별 (메뉴단위)
            NumberExpression<BigDecimal> lineAmt =
                    Expressions.numberTemplate(BigDecimal.class, "COALESCE({0},0)", cod.lineTotal);

            rows = readHints(
                    query.select(Projections.fields(TimeRowDto.class,
                                    ExpressionUtils.as(dayLabel, "date"),
                                    s.name.as("storeName"),
                                    ExpressionUtils.as(hourSlotExpr, "hourSlot"),
                                    ExpressionUtils.as(dayNameExpr, "dayOfWeek"),
                                    co.id.as("orderId"),
                                    ExpressionUtils.as(lineAmt, "orderAmount"),
                                    mc.menuCategoryName.as("category"),
                                    m.menuName.as("menu"),
                                    co.orderType.stringValue().as("orderType"),
                                    ExpressionUtils.as(orderDateStr, "orderDate")
                            ))
                            .from(cod).join(cod.order, co).join(co.storeIdFk, s)
                            .join(cod.menuIdFk, m).join(m.menuCategory, mc)
                            .where(filter)
                            // 🔑 “최근 주문시간 순”을 1순위로만 보장 (나머지는 타이브레이커)
                            .orderBy(
                                    co.orderedAt.desc(),
                                    s.name.asc(),
                                    m.menuName.asc()
                            )
                            .offset(pageable.getOffset())
                            .limit(pageable.getPageSize())
            ).fetch();
        }

        // ③ Total 행 (라벨 × 시간대 × 요일 × OrderType 합계를 "각 블록 위에" 삽입)
        if (!rows.isEmpty() && Boolean.TRUE.equals(cond.getShowTotal())) {

            // 현재 페이지 라벨 수집
            java.util.Set<String> pageLabels = new java.util.LinkedHashSet<>();
            for (TimeRowDto r : rows) pageLabels.add(r.getDate());

            // 라벨 윈도우(현재 페이지 범위)
            BooleanExpression pageWindow = null;
            if (!pageLabels.isEmpty()) {
                if (byMonth) {
                    java.time.YearMonth minYM = null, maxYM = null;
                    for (String lbl : pageLabels) {
                        java.time.YearMonth ym = java.time.YearMonth.parse(lbl);
                        if (minYM == null || ym.isBefore(minYM)) minYM = ym;
                        if (maxYM == null || ym.isAfter(maxYM))  maxYM = ym;
                    }
                    if (minYM != null && maxYM != null) {
                        java.time.LocalDate start = minYM.atDay(1);
                        java.time.LocalDate endPlus1 = maxYM.plusMonths(1).atDay(1);
                        pageWindow = co.orderedAt.goe(start.atStartOfDay())
                                .and(co.orderedAt.lt(endPlus1.atStartOfDay()));
                    }
                } else {
                    java.time.LocalDate minD = null, maxD = null;
                    for (String lbl : pageLabels) {
                        java.time.LocalDate d = java.time.LocalDate.parse(lbl);
                        if (minD == null || d.isBefore(minD)) minD = d;
                        if (maxD == null || d.isAfter(maxD))  maxD = d;
                    }
                    if (minD != null && maxD != null) {
                        pageWindow = co.orderedAt.goe(minD.atStartOfDay())
                                .and(co.orderedAt.lt(maxD.plusDays(1).atStartOfDay()));
                    }
                }
            }

            // 합계 쿼리: (라벨 × 시간(H) × 요일(D) × OrderType) 총액 — 선택 매장 범위 내 전체 합
            StringExpression labelKey = byMonth ? monthLabel : dayLabel;

            // label -> hour -> dow(1~7) -> type -> amt
            Map<String, Map<Integer, Map<Integer, Map<OrderType, BigDecimal>>>> sumMap = new LinkedHashMap<>();

            JPAQuery<Tuple> tq = query.select(
                            labelKey,
                            H, D, co.orderType,
                            Expressions.numberTemplate(BigDecimal.class, "COALESCE(SUM({0}),0)", co.totalPrice)
                    )
                    .from(co)
                    .join(co.storeIdFk, s)
                    .where(filter, pageWindow, H.goe(8).and(H.loe(22)));

            if (byMonth) {
                tq.groupBy(Y, M, H, D, co.orderType).orderBy(orderByNull());
            } else {
                tq.groupBy(co.orderedAt, H, D, co.orderType).orderBy(orderByNull());
            }

            for (Tuple t : readHints(tq).fetch()) {
                String lbl = t.get(0, String.class);
                Integer h  = t.get(1, Integer.class);
                Integer d  = t.get(2, Integer.class);     // 1=일 ... 7=토
                OrderType ot = t.get(3, OrderType.class);
                BigDecimal amt = Optional.ofNullable(t.get(4, BigDecimal.class)).orElse(BigDecimal.ZERO);

                if (lbl == null || h == null || d == null || ot == null) continue;
                if (d < 1 || d > 7) continue;

                sumMap
                        .computeIfAbsent(lbl, k -> new LinkedHashMap<>())
                        .computeIfAbsent(h, k -> new LinkedHashMap<>())
                        .computeIfAbsent(d, k -> new EnumMap<>(OrderType.class))
                        .merge(ot, amt, BigDecimal::add);
            }

            // 기존 rows를 라벨별 → (시간대,요일) 그룹으로 묶고, 각 블록 위에 Total(최대 3줄) 꽂기
            Map<String, List<TimeRowDto>> byLabelAll = new LinkedHashMap<>();
            for (TimeRowDto r : rows) {
                byLabelAll.computeIfAbsent(r.getDate(), k -> new ArrayList<>()).add(r);
            }

            List<TimeRowDto> out = new ArrayList<>(rows.size());

            // 🔧 라벨(날짜/월) 정렬을 **명시적으로 최신→과거**로 맞춤
            List<Map.Entry<String, List<TimeRowDto>>> labelEntries = new ArrayList<>(byLabelAll.entrySet());
            labelEntries.sort((e1, e2) -> {
                if (byMonth) {
                    YearMonth a = YearMonth.parse(e1.getKey());
                    YearMonth b = YearMonth.parse(e2.getKey());
                    return b.compareTo(a); // desc
                } else {
                    LocalDate a = LocalDate.parse(e1.getKey());
                    LocalDate b = LocalDate.parse(e2.getKey());
                    return b.compareTo(a); // desc
                }
            });

            for (Map.Entry<String, List<TimeRowDto>> labelEntry : labelEntries) {
                String label = labelEntry.getKey();
                List<TimeRowDto> labelRows = labelEntry.getValue();

                // (시간대, 요일) 그룹 만들기
                Map<String, List<TimeRowDto>> groups = new LinkedHashMap<>();
                for (TimeRowDto r : labelRows) {
                    String key = (r.getHourSlot()==null ? "-" : r.getHourSlot()) + "|" + (r.getDayOfWeek()==null ? "-" : r.getDayOfWeek());
                    groups.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
                }

                // 그룹 정렬: **시간 22→08**, 같은 시간대 내 **요일 토(7)→일(1)**
                List<Map.Entry<String, List<TimeRowDto>>> gList = new ArrayList<>(groups.entrySet());
                gList.sort(
                        Comparator
                                .comparingInt((Map.Entry<String, List<TimeRowDto>> kv) ->
                                        hourFromSlot(kv.getValue().get(0).getHourSlot())
                                ).reversed()
                                .thenComparing(
                                        Comparator.comparingInt((Map.Entry<String, List<TimeRowDto>> kv) ->
                                                dowIntFromKo(kv.getValue().get(0).getDayOfWeek())
                                        ).reversed()
                                )
                );

                Map<Integer, Map<Integer, Map<OrderType, BigDecimal>>> hMap =
                        sumMap.getOrDefault(label, Map.of());

                for (var g : gList) {
                    List<TimeRowDto> block = g.getValue();
                    if (block.isEmpty()) continue;

                    // 블록 대표값(모든 행 동일)
                    String hourSlot = block.get(0).getHourSlot();
                    String dowKo    = block.get(0).getDayOfWeek();

                    int hVal = hourFromSlot(hourSlot);
                    int dVal = dowIntFromKo(dowKo);

                    Map<OrderType, BigDecimal> tMap =
                            hMap.getOrDefault(hVal, Map.of()).getOrDefault(dVal, Map.of());

                    // OrderType 고정 순서로 최대 3줄 Total 삽입
                    for (OrderType ot : new OrderType[]{OrderType.DELIVERY, OrderType.TAKEOUT, OrderType.VISIT}) {
                        BigDecimal amt = tMap.get(ot);
                        if (amt == null || amt.signum()==0) continue;

                        out.add(TimeRowDto.builder()
                                .date(label)
                                .storeName("Total")
                                .hourSlot(hourSlot)
                                .dayOfWeek(dowKo)
                                .orderId(null)
                                .orderAmount(amt)
                                .category("-")
                                .menu("-")
                                .orderType(ot.name())
                                .orderDate(label)
                                .build());
                    }

                    // 매장 행 정렬: 금액 desc → 점포명 → OrderType (가독성)
                    block.sort(Comparator
                            .comparing((TimeRowDto r) -> r.getOrderAmount()==null ? BigDecimal.ZERO : r.getOrderAmount(), Comparator.reverseOrder())
                            .thenComparing(TimeRowDto::getStoreName, Comparator.nullsLast(String::compareTo))
                            .thenComparing(TimeRowDto::getOrderType, Comparator.nullsLast(String::compareTo))
                    );
                    out.addAll(block);
                }
            }

            return new PageImpl<>(out, pageable, total);
        }

        return new PageImpl<>(rows, pageable, total);
    }

    /* ===================== helpers ===================== */
    private int hourFromSlot(String slot) {
        if (slot == null) return -1;                // ⬅️ 실패 시 맨 아래로
        int idx = slot.indexOf(':');
        if (idx <= 0) return -1;
        try {
            return Integer.parseInt(slot.substring(0, idx));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
    private static String hourSlotText(int h) { return String.format("%02d:00-%02d:59", h, h); }
    private int dowIntFromKo(String dowKo) {
        if (dowKo == null) return Integer.MAX_VALUE;
        return switch (dowKo) {
            case "일" -> 1; case "월" -> 2; case "화" -> 3; case "수" -> 4;
            case "목" -> 5; case "금" -> 6; case "토" -> 7;
            default -> Integer.MAX_VALUE;
        };
    }
    private static String dowKoFromInt(int d) {
        return switch (d) {
            case 1 -> "일"; case 2 -> "월"; case 3 -> "화";
            case 4 -> "수"; case 5 -> "목"; case 6 -> "금";
            case 7 -> "토"; default -> "-";
        };
    }




    /* ===================== 내부 빌더: 차트 공용 ===================== */
    @SuppressWarnings("unchecked")
    private <T> T buildTimeChart(List<Long> storeIds, LocalDate start, LocalDate end, boolean ytdMode) {
        // 08:00 ~ 22:00
        List<String> hours = new java.util.ArrayList<>();
        for (int h = 8; h <= 22; h++) hours.add(String.format("%02d:00", h));
        List<String> dows = java.util.List.of("일","월","화","수","목","금","토");

        BooleanExpression base = co.status.eq(OrderStatus.COMPLETED)
                .and(betweenDateClosedOpen(co.orderedAt, start, end));

        // ⬇️ 필터 모드에서 매장 선택이 있는지 판단
        boolean filteredStores = (!ytdMode && storeIds != null && !storeIds.isEmpty());
        if (filteredStores) {
            base = base.and(s.id.in(storeIds));
        }

        NumberExpression<Integer> H = Expressions.numberTemplate(Integer.class, "HOUR({0})", co.orderedAt);
        NumberExpression<Integer> D = Expressions.numberTemplate(Integer.class, "DAYOFWEEK({0})", co.orderedAt);

        // 합계(선택 범위 기준)
        List<Tuple> hourTotal = readHints(
                query.select(H, co.orderType, co.totalPrice.sum())
                        .from(co).join(co.storeIdFk, s).where(base)
                        .groupBy(H, co.orderType).orderBy(orderByNull())
        ).fetch();
        List<Tuple> dowTotal = readHints(
                query.select(D, co.orderType, co.totalPrice.sum())
                        .from(co).join(co.storeIdFk, s).where(base)
                        .groupBy(D, co.orderType).orderBy(orderByNull())
        ).fetch();

        // 매장별(필터에 매장 선택이 있을 때만)
        List<Tuple> hourByStore = Collections.emptyList();
        List<Tuple> dowByStore  = Collections.emptyList();
        if (filteredStores) {
            hourByStore = readHints(
                    query.select(s.id, s.name, H, co.orderType, co.totalPrice.sum())
                            .from(co).join(co.storeIdFk, s).where(base)
                            .groupBy(s.id, s.name, H, co.orderType).orderBy(orderByNull())
            ).fetch();
            dowByStore = readHints(
                    query.select(s.id, s.name, D, co.orderType, co.totalPrice.sum())
                            .from(co).join(co.storeIdFk, s).where(base)
                            .groupBy(s.id, s.name, D, co.orderType).orderBy(orderByNull())
            ).fetch();
        }

        Map<String,List<BigDecimal>> hourSeries = new LinkedHashMap<>();
        Map<String,List<BigDecimal>> dowSeries  = new LinkedHashMap<>();
        Supplier<List<BigDecimal>> hourZeros = () -> { var z=new ArrayList<BigDecimal>(15); for(int i=0;i<15;i++) z.add(BigDecimal.ZERO); return z; };
        Supplier<List<BigDecimal>> dowZeros  = () -> { var z=new ArrayList<BigDecimal>(7);  for(int i=0;i<7;i++)  z.add(BigDecimal.ZERO); return z; };

        // ✅ “전체 선택” 또는 YTD 카드일 때만 Total 시리즈 추가
        if (!filteredStores) {
            for (Tuple t : hourTotal) {
                Integer h = t.get(0, Integer.class);
                if (h==null || h<8 || h>22) continue;
                String key = "Total - " + t.get(1, OrderType.class).name();
                var arr = hourSeries.computeIfAbsent(key, k -> hourZeros.get());
                arr.set(h - 8, nz(t.get(2, BigDecimal.class)));
            }
            for (Tuple t : dowTotal) {
                Integer d = t.get(0, Integer.class);
                if (d==null || d<1 || d>7) continue;
                String key = "Total - " + t.get(1, OrderType.class).name();
                var arr = dowSeries.computeIfAbsent(key, k -> dowZeros.get());
                arr.set(d - 1, nz(t.get(2, BigDecimal.class)));
            }
        }

        // ✅ 매장 선택 시엔 매장별 시리즈만
        for (Tuple t : hourByStore) {
            String sname = t.get(1, String.class);
            Integer h = t.get(2, Integer.class);
            if (h==null || h<8 || h>22) continue;
            String key = sname + " - " + t.get(3, OrderType.class).name();
            var arr = hourSeries.computeIfAbsent(key, k -> hourZeros.get());
            arr.set(h - 8, nz(t.get(4, BigDecimal.class)));
        }
        for (Tuple t : dowByStore) {
            String sname = t.get(1, String.class);
            Integer d = t.get(2, Integer.class);
            if (d==null || d<1 || d>7) continue;
            String key = sname + " - " + t.get(3, OrderType.class).name();
            var arr = dowSeries.computeIfAbsent(key, k -> dowZeros.get());
            arr.set(d - 1, nz(t.get(4, BigDecimal.class)));
        }

        var hourOut = new ArrayList<ChartSeriesDto>(); hourSeries.forEach((k,v)->hourOut.add(ChartSeriesDto.builder().name(k).data(v).build()));
        var dowOut  = new ArrayList<ChartSeriesDto>(); dowSeries .forEach((k,v)->dowOut .add(ChartSeriesDto.builder().name(k).data(v).build()));

        if (ytdMode) {
            return (T) TimeChartCardDto.builder().hours(hours).dows(dows).timeOfDay(hourOut).dayOfWeek(dowOut).build();
        } else {
            return (T) TimeChartRowDto.builder().hours(hours).dows(dows).timeOfDay(hourOut).dayOfWeek(dowOut).build();
        }
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
                .setHint("jakarta.persistence.query.timeout", 30000);
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

    private <T> T timed(String tag, Supplier<T> fn) {
        long st = System.currentTimeMillis();
        try { return fn.get(); }
        finally { log.info("[PERF][Repo] {}: {} ms", tag, System.currentTimeMillis() - st); }
    }

    private void timedRun(String tag, Runnable fn) {
        long st = System.currentTimeMillis();
        try { fn.run(); }
        finally { log.info("[PERF][Repo] {}: {} ms", tag, System.currentTimeMillis() - st); }
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

    @Override
    public long countKpi(AnalyticsSearchDto cond) {
        boolean byMonth = cond.getViewBy() == ViewBy.MONTH;
        String fmt = byMonth ? "%Y-%m" : "%Y-%m-%d";
        StringExpression labelExpr = dateFormat(co.orderedAt, fmt);
        BooleanExpression baseFilter = eqKpiFilter(cond, co, s);

        StringExpression groupKey = Expressions.stringTemplate(
                "CONCAT_WS('|',{0},{1})", s.id, labelExpr
        );
        return Optional.ofNullable(
                readHints(
                        query.select(Expressions.numberTemplate(Long.class, "COUNT(DISTINCT {0})", groupKey))
                                .from(co).join(co.storeIdFk, s)
                                .where(baseFilter)
                ).fetchOne()
        ).orElse(0L);
    }

    @Override
    public long countOrders(AnalyticsSearchDto cond) {
        boolean byMonth = (cond.getViewBy() == ViewBy.MONTH);
        if (cond.getViewBy() == null) {
            byMonth = false;
        }

        NumberExpression<Integer> yExpr = Expressions.numberTemplate(Integer.class, "YEAR({0})",  co.orderedAt);
        NumberExpression<Integer> mExpr = Expressions.numberTemplate(Integer.class, "MONTH({0})", co.orderedAt);

        BooleanExpression filter = co.status.eq(OrderStatus.COMPLETED);
        if (cond.getStartDate() != null || cond.getEndDate() != null) {
            filter = filter.and(betweenDateClosedOpen(co.orderedAt, cond.getStartDate(), cond.getEndDate()));
        }
        if (cond.getStoreIds() != null && !cond.getStoreIds().isEmpty()) {
            filter = filter.and(s.id.in(cond.getStoreIds()));
        }

        return Optional.ofNullable(
                readHints(
                        byMonth
                                ? query.select(Expressions.numberTemplate(Long.class,
                                        "COUNT(DISTINCT CONCAT_WS('|', {0}, {1}, {2}))", s.id, yExpr, mExpr))
                                .from(co).join(co.storeIdFk, s)
                                .where(filter)
                                : query.select(Expressions.numberTemplate(Long.class,
                                        "COUNT(DISTINCT CONCAT_WS('|', {0}, {1}, {2}, {3}))",
                                        s.id, m.menuId, co.orderedAt, co.orderType))
                                .from(co)
                                .join(cod).on(cod.order.eq(co))
                                .join(co.storeIdFk, s)
                                .join(cod.menuIdFk, m)
                                .where(filter)
                ).fetchOne()
        ).orElse(0L);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTime(AnalyticsSearchDto cond) {
        // 공통 WHERE
        BooleanExpression filter = co.status.eq(OrderStatus.COMPLETED)
                .and(betweenDateClosedOpen(co.orderedAt, cond.getStartDate(), cond.getEndDate()));
        if (cond.getStoreIds() != null && !cond.getStoreIds().isEmpty()) {
            filter = filter.and(s.id.in(cond.getStoreIds()));
        }

        boolean byMonth = (cond.getViewBy() == ViewBy.MONTH);

        NumberExpression<Integer> Y = Expressions.numberTemplate(Integer.class, "YEAR({0})",  co.orderedAt);
        NumberExpression<Integer> M = Expressions.numberTemplate(Integer.class, "MONTH({0})", co.orderedAt);
        NumberExpression<Integer> H = Expressions.numberTemplate(Integer.class, "HOUR({0})",  co.orderedAt);
        NumberExpression<Integer> D = Expressions.numberTemplate(Integer.class, "DAYOFWEEK({0})", co.orderedAt);

        // ✅ findTimeRows()의 GROUP BY와 1:1로 대응
        if (byMonth) {
            // 월별 테이블: from(co) 그룹 (s.id, Y, M, H, D, orderType) + 시간 08~22
            return Optional.ofNullable(
                    readHints(
                            query.select(Expressions.numberTemplate(Long.class,
                                            "COUNT(DISTINCT CONCAT_WS('|',{0},{1},{2},{3},{4},{5}))",
                                            s.id, Y, M, H, D, co.orderType))
                                    .from(co)
                                    .join(co.storeIdFk, s)
                                    .where(filter, H.between(8, 22))
                    ).fetchOne()
            ).orElse(0L);
        } else {
            // 일별 테이블: from(cod) 그룹 (s.id, m.menu_id, orderedAt(원본), orderType)
            return Optional.ofNullable(
                    readHints(
                            query.select(Expressions.numberTemplate(Long.class,
                                            "COUNT(DISTINCT CONCAT_WS('|',{0},{1},{2},{3}))",
                                            s.id, m.menuId, co.orderedAt, co.orderType))
                                    .from(co)
                                    .join(cod).on(cod.order.eq(co))
                                    .join(co.storeIdFk, s)
                                    .join(cod.menuIdFk, m)
                                    .where(filter)
                    ).fetchOne()
            ).orElse(0L);
        }
    }

}