package com.boot.ict05_final_admin.domain.analytics.repository;

import com.boot.ict05_final_admin.domain.analytics.dto.*;
import com.boot.ict05_final_admin.domain.order.entity.OrderStatus;
import com.boot.ict05_final_admin.domain.order.entity.OrderType;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    // ====== 공통 유틸 ======

    /** cond.getStoreIds() 가 비었으면 null(무시), 있으면 IN 절 */
    private BooleanExpression inStores(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) return null;
        // s.storeId 라는 PK 경로를 사용한다고 가정 (필요 시 컬럼명 맞추기)
        return s.id.in(storeIds);
    }

    /** LocalDate 범위를 LocalDateTime(between)으로 변환 (양끝 포함) */
    // 1) 날짜를 TIMESTAMP가 아닌 DATE로 비교 (타임존/시분초 경계 이슈 제거)
    private BooleanExpression betweenDateOnly(DateTimePath<LocalDateTime> path,
                                              LocalDate start, LocalDate end) {
        if (start == null && end == null) return null;
        DateExpression<LocalDate> dateOnly = Expressions.dateTemplate(LocalDate.class, "date({0})", path);
        LocalDate from = (start != null ? start : LocalDate.of(1970,1,1));
        LocalDate to   = (end   != null ? end   : LocalDate.of(9999,12,31));
        return dateOnly.between(from, to);
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

    // ====== 1) KPI 요약카드 : 올해 1/1 ~ 어제 누적 ======
    @Override
    public List<KpiCardsDto> findKpiSummary() {
        LocalDate[] yr = thisYearToYesterday();
        LocalDate start = yr[0];
        LocalDate end   = yr[1];

        BooleanExpression statusDone = co.status.eq(OrderStatus.COMPLETED);
        BooleanExpression period     = betweenDateClosedOpen(co.orderedAt, start, end);

        // 1) 총매출 (SUM)
        BigDecimal totalSalesVal = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co)
                        .where(statusDone, period)
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        // 2) 주문건수 (COUNT(*))
        Long transactionsCnt = Optional.ofNullable(
                query.select(co.id.count())
                        .from(co)
                        .where(statusDone, period)
                        .fetchOne()
        ).orElse(0L);

        // 3) 총판매수량 (상세 합계)
        Integer totalUnitsInt = Optional.ofNullable(
                query.select(cod.quantity.sum())
                        .from(cod)
                        .join(cod.order, co)
                        .where(statusDone, period)
                        .fetchOne()
        ).orElse(0);

        // 4) 채널별 매출
        BigDecimal visitSalesVal = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co)
                        .where(statusDone, period, co.orderType.eq(OrderType.VISIT))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        BigDecimal takeoutSalesVal = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co)
                        .where(statusDone, period, co.orderType.eq(OrderType.TAKEOUT))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        BigDecimal deliverySalesVal = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co)
                        .where(statusDone, period, co.orderType.eq(OrderType.DELIVERY))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        // ===== 파생 KPI (합/나눗셈 방식으로 통일) =====
        BigDecimal trx   = BigDecimal.valueOf(transactionsCnt);
        BigDecimal units = BigDecimal.valueOf(totalUnitsInt.longValue());

        // ADS = 총매출 / 주문건수  (ROUND(…, 2)와 동일)
        BigDecimal adsVal = divOrZero(totalSalesVal, trx, 2);

        // UPT = 총판매수량 / 주문건수 (표시는 소수 6자리까지 유지)
        BigDecimal uptVal = divOrZero(units, trx, 6);

        // AUR = 총매출 / 총판매수량 (ROUND(…, 2)와 동일)
        BigDecimal aurVal = divOrZero(totalSalesVal, units, 2);

        // 채널 비율
        BigDecimal visitRatioVal    = (totalSalesVal.signum()==0) ? BigDecimal.ZERO
                : visitSalesVal.multiply(BigDecimal.valueOf(100))
                .divide(totalSalesVal, 1, RoundingMode.HALF_UP);
        BigDecimal takeoutRatioVal  = (totalSalesVal.signum()==0) ? BigDecimal.ZERO
                : takeoutSalesVal.multiply(BigDecimal.valueOf(100))
                .divide(totalSalesVal, 1, RoundingMode.HALF_UP);
        BigDecimal deliveryRatioVal = (totalSalesVal.signum()==0) ? BigDecimal.ZERO
                : deliverySalesVal.multiply(BigDecimal.valueOf(100))
                .divide(totalSalesVal, 1, RoundingMode.HALF_UP);

        // MoM (이번달 MTD vs 전월 동일일수 MTD) — 날짜는 date() 비교로 경계 오차 제거
        LocalDate today        = LocalDate.now(ZONE_SEOUL);
        LocalDate curMonStart  = today.withDayOfMonth(1);
        LocalDate curMonEnd    = today.minusDays(1);
        LocalDate prevMonStart = curMonStart.minusMonths(1);
        LocalDate prevMonEnd   = prevMonStart.plusDays(curMonEnd.getDayOfMonth()-1);

        BigDecimal curMtd = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co).where(statusDone, betweenDateOnly(co.orderedAt, curMonStart, curMonEnd))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        BigDecimal prevMtd = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co).where(statusDone, betweenDateOnly(co.orderedAt, prevMonStart, prevMonEnd))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        BigDecimal compMoM = (prevMtd.signum()==0) ? BigDecimal.ZERO
                : curMtd.divide(prevMtd, 6, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));

        // YoY (YTD vs 전년동기간)
        LocalDate lastYearStart = start.minusYears(1);
        LocalDate lastYearEnd   = end.minusYears(1);

        BigDecimal lastYtd = Optional.ofNullable(
                query.select(co.totalPrice.sum())
                        .from(co).where(statusDone, betweenDateOnly(co.orderedAt, lastYearStart, lastYearEnd))
                        .fetchOne()
        ).orElse(BigDecimal.ZERO);

        BigDecimal curYtd = totalSalesVal;
        BigDecimal compYoY = (lastYtd.signum()==0) ? BigDecimal.ZERO
                : curYtd.divide(lastYtd, 6, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));

        KpiCardsDto dto = KpiCardsDto.builder()
                .sales(totalSalesVal)
                .transaction(transactionsCnt)
                .upt(uptVal)
                .ads(adsVal)
                .aur(aurVal)
                .compMoM(compMoM)
                .compYoY(compYoY)
                .visitRatio(visitRatioVal)
                .takeoutRatio(takeoutRatioVal)
                .deliveryRatio(deliveryRatioVal)
                .build();

        return List.of(dto);
    }

    @Override
    public List<KpiRowDto> findKpi(AnalyticsSearchDto cond) {
        // KPI 목록(일/월 단위 집계) 조회 - kpi.html 테이블 데이터
        return List.of();
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
    public List<OrdersRowDto> findOrders(AnalyticsSearchDto cond) {
        // 주문 분석 목록(카테고리/메뉴/주문형태 등) 조회 - orders.html 테이블 데이터
        return List.of();
    }

    @Override
    public List<MaterialsRowDto> findMaterials(AnalyticsSearchDto cond) {
        // 재료 분석 목록(재고/발주/소진/원가/마진 등) 조회 - materials.html 테이블 데이터
        return List.of();
    }

    @Override
    public List<TimeRowDto> findTimeSlices(AnalyticsSearchDto cond) {
        // 시간 기반 분석 목록(시간대/요일 등 슬라이스) 조회 - time.html 테이블 데이터
        return List.of();
    }
}
