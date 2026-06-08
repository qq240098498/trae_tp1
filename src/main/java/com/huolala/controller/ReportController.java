package com.huolala.controller;

import com.huolala.dto.DriverPerformanceDetail;
import com.huolala.dto.MonthlyPerformanceReport;
import com.huolala.dto.MonthlyPerformanceReport.LevelDistribution;
import com.huolala.entity.Driver;
import com.huolala.entity.DriverLevel;
import com.huolala.entity.Order;
import com.huolala.entity.Salary;
import com.huolala.service.DriverLevelService;
import com.huolala.service.DriverService;
import com.huolala.service.OrderService;
import com.huolala.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/report")
public class ReportController {
    @Autowired
    private DriverService driverService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private SalaryService salaryService;

    @Autowired
    private DriverLevelService driverLevelService;

    @GetMapping("/monthly")
    public String monthlyReport(Model model, @RequestParam(required = false) String month) {
        if (month == null || month.isEmpty()) {
            month = YearMonth.now().toString();
        }
        model.addAttribute("month", month);

        List<Driver> allDrivers = driverService.findAll();
        List<Salary> salaries = salaryService.findByMonth(month);

        Map<Long, Salary> salaryMap = new HashMap<>();
        for (Salary s : salaries) {
            salaryMap.put(s.getDriver().getId(), s);
        }

        List<DriverPerformanceDetail> details = new ArrayList<>();
        BigDecimal totalFreight = BigDecimal.ZERO;
        BigDecimal totalDriverIncome = BigDecimal.ZERO;
        BigDecimal totalBaseSalary = BigDecimal.ZERO;
        BigDecimal totalLevelBonus = BigDecimal.ZERO;
        BigDecimal totalBonus = BigDecimal.ZERO;
        BigDecimal totalSalaryExpense = BigDecimal.ZERO;
        Integer totalOrders = 0;
        Integer activeDrivers = 0;

        Map<String, List<DriverPerformanceDetail>> levelGroupMap = new HashMap<>();

        for (Driver driver : allDrivers) {
            DriverPerformanceDetail detail = new DriverPerformanceDetail();
            detail.setDriverId(driver.getId());
            detail.setDriverNo(driver.getDriverNo());
            detail.setDriverName(driver.getName());

            DriverLevel driverLevel = null;
            if (driver.getLevelCode() != null) {
                driverLevel = driverLevelService.findByLevelCode(driver.getLevelCode());
            }

            if (driverLevel != null) {
                detail.setLevelName(driverLevel.getLevelName());
                detail.setCommissionRate(driverLevel.getCommissionRate());
            } else {
                detail.setLevelName("未定级");
                detail.setCommissionRate(new BigDecimal("0.70"));
            }

            List<Order> orders = orderService.findCompletedOrdersByDriverAndMonth(driver.getId(), month);
            Integer orderCount = orders.size();

            detail.setOrderCount(orderCount);

            double totalMileage = orders.stream()
                    .filter(o -> o.getDistance() != null)
                    .mapToDouble(Order::getDistance)
                    .sum();
            detail.setTotalMileage(Math.round(totalMileage * 100.0) / 100.0);

            BigDecimal freight = orders.stream()
                    .filter(o -> o.getTotalAmount() != null)
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            detail.setTotalFreight(freight.setScale(2, RoundingMode.HALF_UP));

            Salary salary = salaryMap.get(driver.getId());
            if (salary != null) {
                detail.setOrderIncome(salary.getOrderIncome());
                detail.setBaseSalary(salary.getBaseSalary());
                detail.setLevelBonus(salary.getLevelBonus());
                detail.setBonus(salary.getBonus());
                detail.setDeduction(salary.getDeduction());
                detail.setTotalSalary(salary.getTotalSalary());
                if (salary.getCommissionRate() != null) {
                    detail.setCommissionRate(salary.getCommissionRate());
                }
                if (salary.getLevelName() != null) {
                    detail.setLevelName(salary.getLevelName());
                }
            } else {
                detail.setBaseSalary(driver.getBaseSalary() != null ? driver.getBaseSalary() : BigDecimal.ZERO);
                detail.setOrderIncome(BigDecimal.ZERO);
                detail.setLevelBonus(BigDecimal.ZERO);
                detail.setBonus(BigDecimal.ZERO);
                detail.setDeduction(BigDecimal.ZERO);
                detail.setTotalSalary(detail.getBaseSalary());
            }

            if (orderCount > 0) {
                activeDrivers++;
                totalOrders += orderCount;
                totalFreight = totalFreight.add(detail.getTotalFreight());
                totalDriverIncome = totalDriverIncome.add(detail.getOrderIncome());
                totalBaseSalary = totalBaseSalary.add(detail.getBaseSalary());
                totalLevelBonus = totalLevelBonus.add(detail.getLevelBonus() != null ? detail.getLevelBonus() : BigDecimal.ZERO);
                totalBonus = totalBonus.add(detail.getBonus() != null ? detail.getBonus() : BigDecimal.ZERO);
                totalSalaryExpense = totalSalaryExpense.add(detail.getTotalSalary());
            }

            levelGroupMap.computeIfAbsent(detail.getLevelName(), k -> new ArrayList<>()).add(detail);
            details.add(detail);
        }

        details.sort((d1, d2) -> {
            int c = Integer.compare(
                    d2.getOrderCount() != null ? d2.getOrderCount() : 0,
                    d1.getOrderCount() != null ? d1.getOrderCount() : 0);
            if (c != 0) return c;
            return d2.getTotalSalary().compareTo(d1.getTotalSalary());
        });

        List<LevelDistribution> levelDistributions = new ArrayList<>();
        for (Map.Entry<String, List<DriverPerformanceDetail>> entry : levelGroupMap.entrySet()) {
            LevelDistribution dist = new LevelDistribution();
            dist.setLevelName(entry.getKey());
            dist.setDriverCount(entry.getValue().size());
            if (!entry.getValue().isEmpty()) {
                BigDecimal rate = entry.getValue().get(0).getCommissionRate();
                dist.setCommissionRate(rate);
                BigDecimal avg = entry.getValue().stream()
                        .map(DriverPerformanceDetail::getTotalSalary)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(new BigDecimal(entry.getValue().size()), 2, RoundingMode.HALF_UP);
                dist.setAvgIncome(avg);
            }
            levelDistributions.add(dist);
        }
        levelDistributions.sort((a, b) -> Integer.compare(b.getDriverCount(), a.getDriverCount()));

        MonthlyPerformanceReport report = new MonthlyPerformanceReport();
        report.setMonth(month);
        report.setTotalDrivers(allDrivers.size());
        report.setActiveDrivers(activeDrivers);
        report.setTotalOrders(totalOrders);
        report.setTotalFreight(totalFreight.setScale(2, RoundingMode.HALF_UP));
        report.setTotalDriverIncome(totalDriverIncome.setScale(2, RoundingMode.HALF_UP));
        report.setTotalBaseSalary(totalBaseSalary.setScale(2, RoundingMode.HALF_UP));
        report.setTotalLevelBonus(totalLevelBonus.setScale(2, RoundingMode.HALF_UP));
        report.setTotalBonus(totalBonus.setScale(2, RoundingMode.HALF_UP));
        report.setTotalSalaryExpense(totalSalaryExpense.setScale(2, RoundingMode.HALF_UP));
        report.setDriverDetails(details);
        report.setLevelDistributions(levelDistributions);

        model.addAttribute("report", report);
        model.addAttribute("levels", driverLevelService.findAll());

        return "report/monthly";
    }
}
