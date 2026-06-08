package com.huolala.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverPerformanceDetail {
    private Long driverId;
    private String driverNo;
    private String driverName;
    private String levelName;
    private Integer orderCount;
    private Double totalMileage;
    private BigDecimal totalFreight;
    private BigDecimal commissionRate;
    private BigDecimal orderIncome;
    private BigDecimal baseSalary;
    private BigDecimal levelBonus;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private BigDecimal totalSalary;
}
