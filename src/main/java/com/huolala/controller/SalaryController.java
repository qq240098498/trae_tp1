package com.huolala.controller;

import com.huolala.entity.Salary;
import com.huolala.service.DriverLevelService;
import com.huolala.service.DriverService;
import com.huolala.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/salary")
public class SalaryController {
    @Autowired
    private SalaryService salaryService;
    @Autowired
    private DriverService driverService;
    @Autowired
    private DriverLevelService driverLevelService;

    @GetMapping
    public String list(Model model, @RequestParam(required = false) String month) {
        if (month == null || month.isEmpty()) {
            month = YearMonth.now().toString();
        }
        List<Salary> salaries = salaryService.findByMonth(month);
        model.addAttribute("salaries", salaries);
        model.addAttribute("month", month);
        model.addAttribute("drivers", driverService.findAll());
        model.addAttribute("levels", driverLevelService.findAll());
        return "salary/list";
    }

    @PostMapping("/calculate")
    public String calculate(@RequestParam Long driverId, @RequestParam String month,
                            RedirectAttributes redirectAttributes) {
        try {
            salaryService.calculateSalary(driverId, month);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/salary?month=" + month;
    }

    @PostMapping("/batchCalculate")
    public String batchCalculate(@RequestParam String month) {
        salaryService.batchCalculateSalary(month);
        return "redirect:/salary?month=" + month;
    }
}
