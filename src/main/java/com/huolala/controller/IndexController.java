package com.huolala.controller;

import com.huolala.dto.DriverMileageStats;
import com.huolala.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class IndexController {
    @Autowired
    private DriverService driverService;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private SalaryService salaryService;
    @Autowired
    private FreightConfigService freightConfigService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("driverCount", driverService.findAll().size());
        model.addAttribute("vehicleCount", vehicleService.findAll().size());
        model.addAttribute("orderCount", orderService.findAll().size());
        model.addAttribute("freightCount", freightConfigService.findAll().size());
        model.addAttribute("pendingOrderCount", orderService.findByStatus(0).size());
        
        String currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<DriverMileageStats> monthStats = orderService.getDriverMileageStats(currentMonth, "totalMileage", "desc");
        
        double totalMileage = monthStats.stream()
                .filter(s -> s.getTotalMileage() != null)
                .mapToDouble(DriverMileageStats::getTotalMileage)
                .sum();
        int monthOrderCount = monthStats.stream()
                .filter(s -> s.getOrderCount() != null)
                .mapToInt(DriverMileageStats::getOrderCount)
                .sum();
        
        model.addAttribute("monthTotalMileage", Math.round(totalMileage * 100.0) / 100.0);
        model.addAttribute("monthOrderCount", monthOrderCount);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("topDrivers", monthStats.size() > 3 ? monthStats.subList(0, 3) : monthStats);
        
        return "index";
    }
}
