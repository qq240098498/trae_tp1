package com.huolala.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "order_fee_detail")
public class OrderFeeDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false, length = 50)
    private String feeType;

    @Column(nullable = false, length = 100)
    private String feeName;

    @Column(precision = 10, scale = 2)
    private BigDecimal feeAmount;

    @Column(length = 200)
    private String description;

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
