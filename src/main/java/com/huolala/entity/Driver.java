package com.huolala.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "driver")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String driverNo;

    @Column(nullable = false)
    private String name;

    private String phone;

    private String idCard;

    private String licenseNo;

    private String licenseType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate licenseExpireDate;

    private String address;

    @Column(precision = 10, scale = 2)
    private BigDecimal baseSalary;

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
