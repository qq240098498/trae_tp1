package com.huolala.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPerformanceReport {
    private String month;
    private Integer totalDrivers;
    private Integer activeDrivers;
    private Integer totalOrders;
    private BigDecimal totalFreight;
    private BigDecimal totalDriverIncome;
    private BigDecimal totalBaseSalary;
    private BigDecimal totalLevelBonus;
    private BigDecimal totalBonus;
    private BigDecimal totalSalaryExpense;
    private List<DriverPerformanceDetail> driverDetails;
    private List<LevelDistribution> levelDistributions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelDistribution {
        private String levelName;
        private Integer driverCount;
        private BigDecimal commissionRate;
        private BigDecimal avgIncome;
    }
}
