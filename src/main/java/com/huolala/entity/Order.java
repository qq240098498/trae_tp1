package com.huolala.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderNo;

    @Column(length = 50)
    private String regionCode;

    private String customerName;

    private String customerPhone;

    private String startAddress;

    private String endAddress;

    private Double distance;

    private String vehicleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(precision = 10, scale = 2)
    private BigDecimal freight;

    @Column(precision = 10, scale = 2)
    private BigDecimal carryFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal waitFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal nightSurcharge;

    @Column(precision = 10, scale = 2)
    private BigDecimal floorSurcharge;

    @Column(precision = 10, scale = 2)
    private BigDecimal timeSlotSurcharge;

    @Column(precision = 10, scale = 2)
    private BigDecimal otherSurcharge;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal driverIncome;

    private Integer waitMinutes;

    private Integer floorCount;

    private String timeSlotType;

    private Integer status;

    private String remark;

    @Column(precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(length = 200)
    private String cancelReason;

    private LocalDateTime cancelTime;

    private LocalDateTime orderTime;

    private LocalDateTime acceptTime;

    private LocalDateTime arriveTime;

    private LocalDateTime completeTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
