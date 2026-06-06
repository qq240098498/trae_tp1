package com.huolala.controller;

import com.huolala.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
        return "index";
    }
}
