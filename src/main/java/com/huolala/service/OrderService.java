package com.huolala.service;

import com.huolala.entity.Driver;
import com.huolala.entity.Order;
import com.huolala.entity.Vehicle;
import com.huolala.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FreightConfigService freightConfigService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private VehicleService vehicleService;

    private final Random random = new Random();

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order findById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order findByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo);
    }

    public List<Order> findByStatus(Integer status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> findByDriverId(Long driverId) {
        return orderRepository.findByDriverId(driverId);
    }

    @Transactional
    public Order createOrder(Order order) {
        order.setOrderNo(generateOrderNo());
        order.setStatus(0);
        order.setOrderTime(LocalDateTime.now());

        BigDecimal freight = freightConfigService.calculateFreight(order.getVehicleType(), order.getDistance());
        order.setFreight(freight);

        BigDecimal carryFee = order.getCarryFee() != null ? order.getCarryFee() : BigDecimal.ZERO;
        order.setCarryFee(carryFee);

        order.setWaitFee(BigDecimal.ZERO);
        order.setWaitMinutes(0);
        order.setTotalAmount(freight.add(carryFee));

        BigDecimal driverIncome = order.getTotalAmount().multiply(new BigDecimal("0.80"));
        order.setDriverIncome(driverIncome.setScale(2, BigDecimal.ROUND_HALF_UP));

        return orderRepository.save(order);
    }

    @Transactional
    public Order acceptOrder(Long orderId, Long driverId, Long vehicleId) {
        Order order = findById(orderId);
        if (order == null || order.getStatus() != 0) {
            throw new RuntimeException("订单状态异常，无法接单");
        }

        Driver driver = driverService.findById(driverId);
        Vehicle vehicle = vehicleService.findById(vehicleId);

        order.setDriver(driver);
        order.setVehicle(vehicle);
        order.setStatus(1);
        order.setAcceptTime(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional
    public Order arriveOrder(Long orderId) {
        Order order = findById(orderId);
        if (order == null || order.getStatus() != 1) {
            throw new RuntimeException("订单状态异常，无法确认到达");
        }

        order.setStatus(2);
        order.setArriveTime(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional
    public Order completeOrder(Long orderId, Integer waitMinutes) {
        Order order = findById(orderId);
        if (order == null || order.getStatus() != 2) {
            throw new RuntimeException("订单状态异常，无法完成订单");
        }

        if (waitMinutes != null && waitMinutes > 0) {
            order.setWaitMinutes(waitMinutes);
            BigDecimal waitFee = freightConfigService.calculateWaitFee(order.getVehicleType(), waitMinutes);
            order.setWaitFee(waitFee);
            order.setTotalAmount(order.getTotalAmount().add(waitFee));
            BigDecimal driverIncome = order.getTotalAmount().multiply(new BigDecimal("0.80"));
            order.setDriverIncome(driverIncome.setScale(2, BigDecimal.ROUND_HALF_UP));
        }

        order.setStatus(4);
        order.setCompleteTime(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = findById(orderId);
        if (order == null || order.getStatus() >= 4) {
            throw new RuntimeException("订单状态异常，无法取消");
        }

        order.setStatus(9);
        return orderRepository.save(order);
    }

    public List<Order> findCompletedOrdersByDriverAndMonth(Long driverId, String month) {
        String[] parts = month.split("-");
        int year = Integer.parseInt(parts[0]);
        int monthValue = Integer.parseInt(parts[1]);
        LocalDateTime startTime = LocalDateTime.of(year, monthValue, 1, 0, 0, 0);
        LocalDateTime endTime = startTime.plusMonths(1);
        return orderRepository.findCompletedOrdersByDriverAndDateRange(driverId, startTime, endTime);
    }

    public Integer countCompletedOrdersByDriverAndMonth(Long driverId, String month) {
        String[] parts = month.split("-");
        int year = Integer.parseInt(parts[0]);
        int monthValue = Integer.parseInt(parts[1]);
        LocalDateTime startTime = LocalDateTime.of(year, monthValue, 1, 0, 0, 0);
        LocalDateTime endTime = startTime.plusMonths(1);
        return orderRepository.countCompletedOrdersByDriverAndDateRange(driverId, startTime, endTime);
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = random.nextInt(9000) + 1000;
        return "HL" + dateStr + randomNum;
    }
}
