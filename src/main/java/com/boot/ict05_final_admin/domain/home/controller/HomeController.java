package com.boot.ict05_final_admin.domain.home.controller;

import com.boot.ict05_final_admin.domain.home.dto.DashboardViewDTO;
import com.boot.ict05_final_admin.domain.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/")
    public String home(Model model) {

        DashboardViewDTO viewDTO = homeService.buildDashboard();

        // KPI
        model.addAttribute("kpiRevenueThisMonth",  viewDTO.kpiRevenueThisMonth());
        model.addAttribute("kpiActiveStores",      viewDTO.kpiActiveStores());
        model.addAttribute("kpiOrderCount",        viewDTO.kpiOrderCount());
        model.addAttribute("kpiNewStores",         viewDTO.kpiNewStores());
        model.addAttribute("kpiRevenueGrowthPct",  viewDTO.kpiRevenueGrowthPct());

        // 차트들
        model.addAttribute("salesLabels",   viewDTO.salesLabels());
        model.addAttribute("salesValues",   viewDTO.salesValues());
        model.addAttribute("weeklyLabels",  viewDTO.weeklyLabels());
        model.addAttribute("weeklyValues",  viewDTO.weeklyValues());
        model.addAttribute("logiLabels",    viewDTO.logiLabels());
        model.addAttribute("logiValues",    viewDTO.logiValues());

        // 표
        model.addAttribute("storeRows", viewDTO.storeRows());

        return "index";
    }

}
