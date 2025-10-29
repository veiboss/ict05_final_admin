package com.boot.ict05_final_admin.domain.analytics.controller;

import com.boot.ict05_final_admin.domain.analytics.dto.AnalyticsSearchDto;
import com.boot.ict05_final_admin.domain.analytics.dto.KpiCardsDto;
import com.boot.ict05_final_admin.domain.analytics.dto.KpiRowDto;
import com.boot.ict05_final_admin.domain.analytics.dto.StoreOptionDto;
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

import java.util.List;

/**
 * 본사 통계/분석 화면 컨트롤러.
 *
 * <p>Thymeleaf 페이지 라우팅을 담당한다.</p>
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsRepository analyticsRepository;

    // 매장 목록 모델 주입 (활성 매장만 정렬)
    @ModelAttribute("stores")
    public List<StoreOptionDto> stores() {
        return analyticsRepository.findStoreOptions();
    }

    /**
     * KPI 분석 화면
     */
    @GetMapping("/kpi")
    public String kpi(AnalyticsSearchDto analyticsSearchDto,
                      @PageableDefault(page = 1, size = 50) Pageable pageable,
                      Model model,
                      HttpServletRequest request) {

        PageRequest pageRequest = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize()
        );

        AnalyticsSearchDto cond = AnalyticsSearchDto.withDefaults(analyticsSearchDto);
        Page<KpiRowDto> kpirows = analyticsService.selectKpis(cond, pageRequest);
        KpiCardsDto card = analyticsService.selectKpiCards();

        model.addAttribute("analyticsSearchDto", cond);
        model.addAttribute("kpirows", kpirows);
        model.addAttribute("kpiCard", card);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        System.out.println("그냥 컨트롤러 호출");
        return "analytics/kpi";
    }

    /**
     * 주문 분석 화면
     */
    @GetMapping("/orders")
    public String orders(AnalyticsSearchDto search, Model model) {
        model.addAttribute("search", AnalyticsSearchDto.withDefaults(search));
        return "analytics/orders";
    }

    /**
     * 재료 분석 화면
     */
    @GetMapping("/materials")
    public String materials(AnalyticsSearchDto search, Model model) {
        model.addAttribute("search", AnalyticsSearchDto.withDefaults(search));
        return "analytics/materials";
    }

    /**
     * 시간·요일 분석 화면
     */
    @GetMapping("/time")
    public String time(AnalyticsSearchDto search, Model model) {
        model.addAttribute("search", AnalyticsSearchDto.withDefaults(search));
        return "analytics/time";
    }
}
