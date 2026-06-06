package com.huolala.repository;

import com.huolala.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByOrderNo(String orderNo);
    List<Order> findByStatus(Integer status);
    List<Order> findByDriverId(Long driverId);

    @Query("SELECT o FROM Order o WHERE o.driver.id = :driverId AND o.completeTime BETWEEN :startTime AND :endTime AND o.status = 4")
    List<Order> findCompletedOrdersByDriverAndDateRange(
            @Param("driverId") Long driverId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT COUNT(o) FROM Order o WHERE o.driver.id = :driverId AND o.completeTime BETWEEN :startTime AND :endTime AND o.status = 4")
    Integer countCompletedOrdersByDriverAndDateRange(
            @Param("driverId") Long driverId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
