package com.huolala.controller;

import com.huolala.common.Result;
import com.huolala.dto.DriverMileageStats;
import com.huolala.entity.Order;
import com.huolala.service.DriverService;
import com.huolala.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private DriverService driverService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("driverCount", driverService.findAll().size());
        data.put("orderCount", orderService.findAll().size());
        data.put("pendingOrderCount", orderService.findByStatus(0).size());
        return success(data);
    }

    @GetMapping("/orders")
    public Result<List<Order>> getOrders() {
        List<Order> orders = orderService.findAll();
        return success(orders);
    }

    @GetMapping("/orders/{id}")
    public Result<Order> getOrder(@PathVariable Long id) {
        Order order = orderService.findById(id);
        if (order == null) {
            return error(404, "订单不存在");
        }
        return success(order);
    }

    @GetMapping("/stats/driver-mileage")
    public Result<List<DriverMileageStats>> getDriverMileageStats(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {

        if (month == null || month.isEmpty()) {
            month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "totalMileage";
        }
        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = "desc";
        }

        List<DriverMileageStats> stats = orderService.getDriverMileageStats(month, sortBy, sortOrder);
        return success(stats);
    }
}
