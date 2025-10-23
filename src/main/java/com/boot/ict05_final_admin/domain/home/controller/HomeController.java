package com.boot.ict05_final_admin.domain.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {

        // KPI 값을 안쓰는중
        model.addAttribute("kpiRevenueThisMonth", 28_400_000L);
        model.addAttribute("kpiActiveStores", 18);
        model.addAttribute("kpiOrderCount", 1_247);
        model.addAttribute("kpiNewStores", 5);
        model.addAttribute("kpiRevenueGrowthPct", 12.5);

        // 월별 매출 추이
        model.addAttribute("salesLabels", List.of("1월","2월","3월","4월","5월","6월"));
        model.addAttribute("salesValues", List.of(4200, 3800, 5200, 4800, 6100, 5900));

        // 주간 매출
        model.addAttribute("weeklyLabels", List.of("월","화","수","목","금","토","일"));
        model.addAttribute("weeklyValues", List.of(1200, 980, 1450, 1100, 1800, 2200, 1900));

        // 물류 매출
        model.addAttribute("logiLabels", List.of("1월","2월","3월","4월","5월","6월"));
        model.addAttribute("logiValues", List.of(8_500_000, 7_800_000, 9_200_000, 10_100_000, 11_300_000, 12_800_000));

        // 매장별 매출
        record StoreRow(String store, long revenue, double growth) {}
        model.addAttribute("storeRows", List.of(
                new StoreRow("신촌점", 5_200_000, 12.5),
                new StoreRow("홍대점", 4_800_000, 8.3),
                new StoreRow("강남점", 6_100_000, 15.2),
                new StoreRow("건대점", 4_300_000, 5.7),
                new StoreRow("이대점", 3_900_000, -2.1)
        ));

        return "index";
    }

}
