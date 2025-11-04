package com.boot.ict05_final_admin.domain.analytics.repository;

import com.boot.ict05_final_admin.domain.analytics.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnalyticsRepository {

    List<StoreOptionDto> findStoreOptions();

    KpiCardsDto findKpiSummary();

    OrdersCardsDto findOrdersSummary();

    MaterialsCardsDto findMaterialsSummary();

    Page<KpiRowDto> findKpi(AnalyticsSearchDto cond, Pageable pageable);

    long countKpi(AnalyticsSearchDto cond);

    Page<OrdersRowDto> findOrders(AnalyticsSearchDto cond, Pageable pageable);

    long countOrders(AnalyticsSearchDto cond);

    Page<MaterialsRowDto> findMaterials(AnalyticsSearchDto cond, Pageable pageable);

    TimeChartCardDto findTimeChartSummary();
    TimeChartRowDto  findTimeChart(AnalyticsSearchDto cond);
    Page<TimeRowDto> findTimeRows(AnalyticsSearchDto cond, Pageable pageable);
}