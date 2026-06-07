package com.huolala.service;

import com.huolala.dto.DriverMileageStats;
import com.huolala.dto.FreightCalculationResult;
import com.huolala.entity.Driver;
import com.huolala.entity.Order;
import com.huolala.entity.OrderFeeDetail;
import com.huolala.entity.Vehicle;
import com.huolala.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FreightConfigService freightConfigService;

    @Autowired
    private OrderFeeDetailService orderFeeDetailService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private DriverStatsRedisService driverStatsRedisService;

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

        String timeSlotType = freightConfigService.getTimeSlotType(order.getOrderTime(), order.getVehicleType());
        order.setTimeSlotType(timeSlotType);

        FreightCalculationResult calcResult = freightConfigService.calculateFreight(
                order.getVehicleType(),
                order.getDistance(),
                order.getFloorCount(),
                order.getOrderTime(),
                order.getCarryFee(),
                order.getOtherSurcharge()
        );

        order.setFreight(calcResult.getBaseFreight().add(calcResult.getMileageFee()));
        order.setCarryFee(calcResult.getCarryFee());
        order.setNightSurcharge(calcResult.getNightSurcharge());
        order.setFloorSurcharge(calcResult.getFloorSurcharge());
        order.setTimeSlotSurcharge(calcResult.getTimeSlotSurcharge());
        order.setOtherSurcharge(calcResult.getOtherSurcharge());
        order.setWaitFee(BigDecimal.ZERO);
        order.setWaitMinutes(0);
        order.setTotalAmount(calcResult.getTotalAmount());

        BigDecimal driverIncome = order.getTotalAmount().multiply(new BigDecimal("0.80"));
        order.setDriverIncome(driverIncome.setScale(2, BigDecimal.ROUND_HALF_UP));

        Order savedOrder = orderRepository.save(order);

        for (OrderFeeDetail detail : calcResult.getFeeDetails()) {
            detail.setOrderId(savedOrder.getId());
        }
        orderFeeDetailService.saveBatch(calcResult.getFeeDetails());

        return savedOrder;
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

            OrderFeeDetail waitDetail = freightConfigService.createWaitFeeDetail(
                    orderId, order.getVehicleType(), waitMinutes);
            orderFeeDetailService.save(waitDetail);
        }

        order.setStatus(4);
        order.setCompleteTime(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        driverStatsRedisService.updateDriverStats(savedOrder);

        return savedOrder;
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

    public List<DriverMileageStats> getDriverMileageStats(String month, String sortBy, String sortOrder) {
        List<DriverMileageStats> redisStats = driverStatsRedisService.getDriverMileageStatsFromRedis(month, sortBy, sortOrder);
        if (redisStats != null && !redisStats.isEmpty()) {
            return redisStats;
        }

        List<Driver> drivers = driverService.findAll();
        List<DriverMileageStats> statsList = new ArrayList<>();

        for (Driver driver : drivers) {
            List<Order> orders = findCompletedOrdersByDriverAndMonth(driver.getId(), month);
            
            if (orders.isEmpty()) {
                continue;
            }

            double totalMileage = orders.stream()
                    .filter(o -> o.getDistance() != null)
                    .mapToDouble(Order::getDistance)
                    .sum();

            int orderCount = orders.size();

            BigDecimal totalIncome = orders.stream()
                    .filter(o -> o.getDriverIncome() != null)
                    .map(Order::getDriverIncome)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalFreight = orders.stream()
                    .filter(o -> o.getTotalAmount() != null)
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double avgMileage = orderCount > 0 ? totalMileage / orderCount : 0;

            DriverMileageStats stats = new DriverMileageStats();
            stats.setDriverId(driver.getId());
            stats.setDriverName(driver.getName());
            stats.setDriverNo(driver.getDriverNo());
            stats.setTotalMileage(Math.round(totalMileage * 100.0) / 100.0);
            stats.setOrderCount(orderCount);
            stats.setTotalIncome(totalIncome.setScale(2, BigDecimal.ROUND_HALF_UP));
            stats.setTotalFreight(totalFreight.setScale(2, BigDecimal.ROUND_HALF_UP));
            stats.setAvgMileagePerOrder(Math.round(avgMileage * 100.0) / 100.0);

            statsList.add(stats);
        }

        if (sortBy != null && !sortBy.isEmpty()) {
            boolean ascending = "asc".equalsIgnoreCase(sortOrder);
            statsList.sort((s1, s2) -> {
                int result = 0;
                switch (sortBy) {
                    case "totalMileage":
                        result = Double.compare(s1.getTotalMileage(), s2.getTotalMileage());
                        break;
                    case "orderCount":
                        result = Integer.compare(s1.getOrderCount(), s2.getOrderCount());
                        break;
                    case "totalIncome":
                        result = s1.getTotalIncome().compareTo(s2.getTotalIncome());
                        break;
                    case "totalFreight":
                        result = s1.getTotalFreight().compareTo(s2.getTotalFreight());
                        break;
                    case "avgMileagePerOrder":
                        result = Double.compare(s1.getAvgMileagePerOrder(), s2.getAvgMileagePerOrder());
                        break;
                    default:
                        result = Double.compare(s2.getTotalMileage(), s1.getTotalMileage());
                }
                return ascending ? result : -result;
            });
        } else {
            statsList.sort((s1, s2) -> Double.compare(s2.getTotalMileage(), s1.getTotalMileage()));
        }

        if (!statsList.isEmpty()) {
            driverStatsRedisService.rebuildStats(month, statsList);
        }

        return statsList;
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = random.nextInt(9000) + 1000;
        return "HL" + dateStr + randomNum;
    }
}
