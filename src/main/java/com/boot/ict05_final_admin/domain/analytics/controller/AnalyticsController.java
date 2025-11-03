package com.boot.ict05_final_admin.domain.analytics.controller;

import com.boot.ict05_final_admin.domain.analytics.dto.*;
import com.boot.ict05_final_admin.domain.analytics.repository.AnalyticsRepository;
import com.boot.ict05_final_admin.domain.analytics.service.AnalyticsService;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

/**
 * 본사 통계/분석 화면 컨트롤러.
 *
 * <p>Thymeleaf 기반의 분석 페이지 라우팅을 담당한다.
 * KPI / 주문 / 재료 / 시간·요일 분석 화면으로의 진입과,
 * 공통 검색 조건의 초기값 주입을 처리한다.</p>
 *
 * @author
 * @since 1.0
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsRepository analyticsRepository;

    /**
     * 공통 모델 속성: 가맹점(점포) 선택 옵션 목록.
     *
     * <p>활성 매장만 정렬된 형태로 제공하여, 화면의 셀렉트 박스에서 사용한다.</p>
     *
     * @return 점포 옵션 목록
     */
    @ModelAttribute("stores")
    public List<StoreOptionDto> stores() {
        return analyticsRepository.findStoreOptions();
    }

    /**
     * KPI 분석 화면.
     *
     * <p>검색 기간이 비어 있으면 기본값으로 "어제 기준 최근 7일"을 주입한다.
     * 페이징은 1-based로 들어오는 값을 0-based로 보정하여 PageRequest를 생성한다.</p>
     *
     * @param analyticsSearchDto 검색 조건 DTO
     * @param pageable           페이지/사이즈 정보
     * @param model              뷰 모델
     * @param request            요청(현재 URL 보존용)
     * @return KPI 템플릿 경로 ("analytics/kpi")
     */
    @GetMapping("/kpi")
    public String viewKpiAnalysis(AnalyticsSearchDto analyticsSearchDto,
                                  @PageableDefault(page = 1, size = 50) Pageable pageable,
                                  Model model,
                                  HttpServletRequest request) {

        PageRequest pageRequest = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize()
        );

        if (analyticsSearchDto.getStartDate() == null || analyticsSearchDto.getEndDate() == null) {
            LocalDate end = LocalDate.now().minusDays(1);
            LocalDate start = end.minusDays(6);
            analyticsSearchDto.setStartDate(start);
            analyticsSearchDto.setEndDate(end);
        }

        AnalyticsSearchDto cond = AnalyticsSearchDto.withDefaults(analyticsSearchDto);
        Page<KpiRowDto> kpirows = analyticsService.selectKpis(cond, pageRequest);
        KpiCardsDto card = analyticsService.selectKpiCards();

        model.addAttribute("analyticsSearchDto", cond);
        model.addAttribute("kpirows", kpirows);
        model.addAttribute("kpiCard", card);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        return "analytics/kpi";
    }

    /**
     * 주문 분석 화면.
     *
     * <p>검색 기간이 비어 있으면 기본값으로 "어제 기준 최근 7일"을 주입한다.
     * 상단 카드(OrdersCardsDto)는 YTD 기준이며, 목록은 조건/페이징에 따른다.</p>
     *
     * @param analyticsSearchDto 검색 조건 DTO
     * @param pageable           페이지/사이즈 정보
     * @param model              뷰 모델
     * @param request            요청(현재 URL 보존용)
     * @return 주문 분석 템플릿 경로 ("analytics/orders")
     */
    @GetMapping("/orders")
    public String viewOrdersAnalysis(AnalyticsSearchDto analyticsSearchDto,
                                     @PageableDefault(page = 1, size = 50) Pageable pageable,
                                     Model model,
                                     HttpServletRequest request) {

        PageRequest pageRequest = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize()
        );

        if (analyticsSearchDto.getStartDate() == null || analyticsSearchDto.getEndDate() == null) {
            LocalDate end = LocalDate.now().minusDays(1);
            LocalDate start = end.minusDays(6);
            analyticsSearchDto.setStartDate(start);
            analyticsSearchDto.setEndDate(end);
        }

        AnalyticsSearchDto cond = AnalyticsSearchDto.withDefaults(analyticsSearchDto);
        Page<OrdersRowDto> orderrows = analyticsService.selectOrders(analyticsSearchDto, pageRequest);
        OrdersCardsDto card = analyticsService.selectOrdersCards();

        model.addAttribute("analyticsSearchDto", cond);
        model.addAttribute("orderrows", orderrows);
        model.addAttribute("orderCard", card);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        return "analytics/orders";
    }

    /**
     * 재료 분석 화면.
     *
     * <p>검색 기간이 비어 있으면 기본값으로 "어제 기준 최근 7일"을 주입한다.
     * 상단 카드(MaterialsCardsDto)는 YTD 기준이며, 목록은 조건/페이징에 따른다.</p>
     *
     * @param analyticsSearchDto 검색 조건 DTO
     * @param pageable           페이지/사이즈 정보
     * @param model              뷰 모델
     * @param request            요청(현재 URL 보존용)
     * @return 재료 분석 템플릿 경로 ("analytics/materials")
     */
    @GetMapping("/materials")
    public String viewMaterialsAnalysis(AnalyticsSearchDto analyticsSearchDto,
                            @PageableDefault(page = 1, size = 50) Pageable pageable,
                            Model model,
                            HttpServletRequest request) {

        PageRequest pageRequest = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize()
        );

        if (analyticsSearchDto.getStartDate() == null || analyticsSearchDto.getEndDate() == null) {
            LocalDate end = LocalDate.now().minusDays(1);
            LocalDate start = end.minusDays(6);
            analyticsSearchDto.setStartDate(start);
            analyticsSearchDto.setEndDate(end);
        }

        AnalyticsSearchDto cond = AnalyticsSearchDto.withDefaults(analyticsSearchDto);
        Page<MaterialsRowDto> page = analyticsService.selectMaterials(cond, pageRequest);
        MaterialsCardsDto card = analyticsService.selectMaterialsCards();

        model.addAttribute("analyticsSearchDto", cond);
        model.addAttribute("materialrows", page);
        model.addAttribute("materialCard", card);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        return "analytics/materials";
    }

    /**
     * 시간·요일 분석 화면.

     */
    @GetMapping("/time")
    public String viewTimeAnalysis(AnalyticsSearchDto analyticsSearchDto,
                                   @PageableDefault(page = 1, size = 40) Pageable pageable,
                                   Model model,
                                   HttpServletRequest request) {

        // 기간 기본값: 어제 기준 최근 7일
        if (analyticsSearchDto.getStartDate() == null || analyticsSearchDto.getEndDate() == null) {
            LocalDate end = LocalDate.now().minusDays(1);
            LocalDate start = end.minusDays(6);
            analyticsSearchDto.setStartDate(start);
            analyticsSearchDto.setEndDate(end);
        }

        PageRequest pageRequest = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize(),
                pageable.getSort()
        );

        AnalyticsSearchDto cond = AnalyticsSearchDto.withDefaults(analyticsSearchDto);

        // ✅ SSR: 차트 2개 + 표 페이지
        TimeChartCardDto timeCard  = analyticsService.selectTimeChartCards(); // YTD
        TimeChartRowDto  timeChart = analyticsService.selectTimeChart(cond);  // 필터 적용
        Page<TimeRowDto> timerows  = analyticsService.selectTimeRows(cond, pageRequest);

        model.addAttribute("analyticsSearchDto", cond);
        model.addAttribute("timeCard",  timeCard);
        model.addAttribute("timeChart", timeChart);
        model.addAttribute("timerows",  timerows);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        return "analytics/time";
    }
}
