package com.huolala.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "driver_level")
public class DriverLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer levelCode;

    @Column(nullable = false, length = 50)
    private String levelName;

    @Column(nullable = false)
    private Integer minOrders;

    private Integer maxOrders;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal commissionRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal levelBonus;

    @Column(length = 200)
    private String description;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
