package com.huolala.service;

import com.huolala.entity.OrderFeeDetail;
import com.huolala.repository.OrderFeeDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderFeeDetailService {
    @Autowired
    private OrderFeeDetailRepository orderFeeDetailRepository;

    public List<OrderFeeDetail> findByOrderId(Long orderId) {
        return orderFeeDetailRepository.findByOrderId(orderId);
    }

    @Transactional
    public OrderFeeDetail save(OrderFeeDetail detail) {
        return orderFeeDetailRepository.save(detail);
    }

    @Transactional
    public void saveBatch(List<OrderFeeDetail> details) {
        orderFeeDetailRepository.saveAll(details);
    }

    @Transactional
    public void deleteByOrderId(Long orderId) {
        orderFeeDetailRepository.deleteByOrderId(orderId);
    }
}
