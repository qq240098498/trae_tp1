package com.huolala.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "freight_config")
public class FreightConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleType;

    @Column(precision = 10, scale = 2)
    private BigDecimal startPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal startDistance;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerKm;

    @Column(precision = 10, scale = 2)
    private BigDecimal waitPricePerMin;

    @Column(precision = 10, scale = 2)
    private BigDecimal carryPrice;

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
