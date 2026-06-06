package com.huolala.repository;

import com.huolala.entity.OrderFeeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderFeeDetailRepository extends JpaRepository<OrderFeeDetail, Long> {
    List<OrderFeeDetail> findByOrderId(Long orderId);
    void deleteByOrderId(Long orderId);
}
