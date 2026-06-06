package com.huolala.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverMileageStats {
    private Long driverId;
    private String driverName;
    private String driverNo;
    private Double totalMileage;
    private Integer orderCount;
    private BigDecimal totalIncome;
    private BigDecimal totalFreight;
    private Double avgMileagePerOrder;
}
