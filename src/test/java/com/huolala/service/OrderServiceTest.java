package com.huolala.service;

import com.huolala.dto.DriverMileageStats;
import com.huolala.dto.FreightCalculationResult;
import com.huolala.entity.Driver;
import com.huolala.entity.Order;
import com.huolala.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private FreightConfigService freightConfigService;

    @Mock
    private OrderFeeDetailService orderFeeDetailService;

    @Mock
    private DriverService driverService;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private OrderService orderService;

    private Driver driver1;
    private Driver driver2;
    private List<Order> driver1Orders;
    private List<Order> driver2Orders;

    @BeforeEach
    void setUp() {
        driver1 = new Driver();
        driver1.setId(1L);
        driver1.setName("张三");
        driver1.setDriverNo("DRV001");

        driver2 = new Driver();
        driver2.setId(2L);
        driver2.setName("李四");
        driver2.setDriverNo("DRV002");

        driver1Orders = createDriverOrders(1L, 5);
        driver2Orders = createDriverOrders(2L, 3);
    }

    private List<Order> createDriverOrders(Long driverId, int count) {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Order order = new Order();
            order.setId((long) (driverId * 100 + i));
            order.setDriver(driverId == 1 ? driver1 : driver2);
            order.setDistance(10.0 + i * 2.5);
            order.setTotalAmount(new BigDecimal("100").add(new BigDecimal(i * 20)));
            order.setDriverIncome(new BigDecimal("80").add(new BigDecimal(i * 16)));
            order.setStatus(4);
            order.setCompleteTime(LocalDateTime.of(2024, 6, 10 + i, 14, 0));
            order.setNightSurcharge(new BigDecimal("10"));
            order.setFloorSurcharge(new BigDecimal("15"));
            order.setCarryFee(new BigDecimal("50"));
            orders.add(order);
        }
        return orders;
    }

    @Test
    @DisplayName("TC-STATS-001: 司机里程统计 - 单司机统计")
    void testGetDriverMileageStats_SingleDriver() {
        when(driverService.findAll()).thenReturn(Arrays.asList(driver1));
        when(orderRepository.findCompletedOrdersByDriverAndDateRange(eq(1L), any(), any()))
                .thenReturn(driver1Orders);

        List<DriverMileageStats> stats = orderService.getDriverMileageStats("2024-06", "totalMileage", "desc");

        assertEquals(1, stats.size());
        DriverMileageStats stat = stats.get(0);
        assertEquals("张三", stat.getDriverName());
        assertEquals("DRV001", stat.getDriverNo());
        assertEquals(5, stat.getOrderCount());
        assertEquals(75.0, stat.getTotalMileage());
        assertEquals(new BigDecimal("560.00"), stat.getTotalIncome());
        assertEquals(new BigDecimal("700.00"), stat.getTotalFreight());
        assertEquals(15.0, stat.getAvgMileagePerOrder());
    }

    @Test
    @DisplayName("TC-STATS-002: 司机里程统计 - 多司机排序（按总里程降序）")
    void testGetDriverMileageStats_MultipleDrivers_SortByMileageDesc() {
        when(driverService.findAll()).thenReturn(Arrays.asList(driver1, driver2));
        when(orderRepository.findCompletedOrdersByDriverAndDateRange(eq(1L), any(), any()))
                .thenReturn(driver1Orders);
        when(orderRepository.findCompletedOrdersByDriverAndDateRange(eq(2L), any(), any()))
                .thenReturn(driver2Orders);

        List<DriverMileageStats> stats = orderService.getDriverMileageStats("2024-06", "totalMileage", "desc");

        assertEquals(2, stats.size());
        assertEquals("张三", stats.get(0).getDriverName());
        assertEquals("李四", stats.get(1).getDriverName());
        assertTrue(stats.get(0).getTotalMileage() > stats.get(1).getTotalMileage());
    }

    @Test
    @DisplayName("TC-STATS-003: 司机里程统计 - 按订单数排序")
    void testGetDriverMileageStats_SortByOrderCount() {
        when(driverService.findAll()).thenReturn(Arrays.asList(driver1, driver2));
        when(orderRepository.findCompletedOrdersByDriverAndDateRange(eq(1L), any(), any()))
                .thenReturn(driver1Orders);
        when(orderRepository.findCompletedOrdersByDriverAndDateRange(eq(2L), any(), any()))
                .thenReturn(driver2Orders);

        List<DriverMileageStats> stats = orderService.getDriverMileageStats("2024-06", "orderCount", "desc");

        assertEquals(5, stats.get(0).getOrderCount());
        assertEquals(3, stats.get(1).getOrderCount());
    }

    @Test
    @DisplayName("TC-STATS-004: 司机里程统计 - 按总收入排序")
    void testGetDriverMileageStats_SortByTotalIncome() {
        when(driverService.findAll()).thenReturn(Arrays.asList(driver1, driver2));
        when(orderRepository.findCompletedOrdersByDriverAndDateRange(eq(1L), any(), any()))
                .thenReturn(driver1Orders);
        when(orderRepository.findCompletedOrdersByDriverAndDateRange(eq(2L), any(), any()))
                .thenReturn(driver2Orders);

        List<DriverMileageStats> stats = orderService.getDriverMileageStats("2024-06", "totalIncome", "desc");

        assertTrue(stats.get(0).getTotalIncome().compareTo(stats.get(1).getTotalIncome()) > 0);
    }

    @Test
    @DisplayName("TC-STATS-005: 司机里程统计 - 按平均里程排序")
    void testGetDriverMileageStats_SortByAvgMileage() {
        when(driverService.findAll()).thenReturn(Arrays.asList(driver1, driver2));
        when(orderRepository.findCompletedOrdersByDriverAndDateRange(eq(1L), any(), any()))
                .thenReturn(driver1Orders);
        when(orderRepository.findCompletedOrdersByDriverAndDateRange(eq(2L), any(), any()))
                .thenReturn(driver2Orders);

        List<DriverMileageStats> stats = orderService.getDriverMileageStats("2024-06", "avgMileagePerOrder", "asc");

        assertTrue(stats.get(0).getAvgMileagePerOrder() <= stats.get(1).getAvgMileagePerOrder());
    }

    @Test
    @DisplayName("TC-STATS-006: 司机里程统计 - 无订单的司机不显示")
    void testGetDriverMileageStats_NoOrdersDriver_Excluded() {
        Driver noOrderDriver = new Driver();
        noOrderDriver.setId(3L);
        noOrderDriver.setName("王五");
        noOrderDriver.setDriverNo("DRV003");

        when(driverService.findAll()).thenReturn(Arrays.asList(driver1, noOrderDriver));
        when(orderRepository.findCompletedOrdersByDriverAndDateRange(eq(1L), any(), any()))
                .thenReturn(driver1Orders);
        when(orderRepository.findCompletedOrdersByDriverAndDateRange(eq(3L), any(), any()))
                .thenReturn(new ArrayList<>());

        List<DriverMileageStats> stats = orderService.getDriverMileageStats("2024-06", "totalMileage", "desc");

        assertEquals(1, stats.size());
        assertEquals("张三", stats.get(0).getDriverName());
    }

    @Test
    @DisplayName("TC-STATS-007: 司机里程统计 - 按总运费排序")
    void testGetDriverMileageStats_SortByTotalFreight() {
        when(driverService.findAll()).thenReturn(Arrays.asList(driver1, driver2));
        when(orderRepository.findCompletedOrdersByDriverAndDateRange(eq(1L), any(), any()))
                .thenReturn(driver1Orders);
        when(orderRepository.findCompletedOrdersByDriverAndDateRange(eq(2L), any(), any()))
                .thenReturn(driver2Orders);

        List<DriverMileageStats> stats = orderService.getDriverMileageStats("2024-06", "totalFreight", "desc");

        assertTrue(stats.get(0).getTotalFreight().compareTo(stats.get(1).getTotalFreight()) > 0);
    }

    @Test
    @DisplayName("TC-ORDER-001: 创建订单 - 运费计算包含夜间加费")
    void testCreateOrder_WithNightSurcharge() {
        Order order = new Order();
        order.setVehicleType("小面包车");
        order.setDistance(10.0);
        order.setFloorCount(3);
        order.setCarryFee(new BigDecimal("50"));
        order.setOtherSurcharge(new BigDecimal("20"));

        FreightCalculationResult calcResult = new FreightCalculationResult();
        calcResult.setBaseFreight(new BigDecimal("35"));
        calcResult.setMileageFee(new BigDecimal("15"));
        calcResult.setNightSurcharge(new BigDecimal("10"));
        calcResult.setFloorSurcharge(new BigDecimal("15"));
        calcResult.setCarryFee(new BigDecimal("50"));
        calcResult.setOtherSurcharge(new BigDecimal("20"));
        calcResult.calculateTotal();

        when(freightConfigService.getTimeSlotType(any(), eq("小面包车"))).thenReturn("NIGHT");
        when(freightConfigService.calculateFreight(
                eq("小面包车"), eq(10.0), eq(3), any(), eq(new BigDecimal("50")), eq(new BigDecimal("20"))))
                .thenReturn(calcResult);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(orderFeeDetailService.saveBatch(anyList())).thenReturn(null);

        Order result = orderService.createOrder(order);

        assertNotNull(result);
        assertEquals(new BigDecimal("50.00"), result.getFreight());
        assertEquals(new BigDecimal("10.00"), result.getNightSurcharge());
        assertEquals(new BigDecimal("15.00"), result.getFloorSurcharge());
        assertEquals(new BigDecimal("50.00"), result.getCarryFee());
        assertEquals(new BigDecimal("20.00"), result.getOtherSurcharge());
        assertEquals(new BigDecimal("145.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("116.00"), result.getDriverIncome());
        assertEquals("NIGHT", result.getTimeSlotType());
    }

    @Test
    @DisplayName("TC-ORDER-002: 创建订单 - 正常时段无夜间加费")
    void testCreateOrder_NoNightSurcharge_Daytime() {
        Order order = new Order();
        order.setVehicleType("小面包车");
        order.setDistance(5.0);

        FreightCalculationResult calcResult = new FreightCalculationResult();
        calcResult.setBaseFreight(new BigDecimal("35"));
        calcResult.setNightSurcharge(BigDecimal.ZERO);
        calcResult.calculateTotal();

        when(freightConfigService.getTimeSlotType(any(), eq("小面包车"))).thenReturn("NORMAL");
        when(freightConfigService.calculateFreight(
                eq("小面包车"), eq(5.0), isNull(), any(), isNull(), isNull()))
                .thenReturn(calcResult);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(orderFeeDetailService.saveBatch(anyList())).thenReturn(null);

        Order result = orderService.createOrder(order);

        assertEquals(BigDecimal.ZERO.setScale(2), result.getNightSurcharge());
        assertEquals("NORMAL", result.getTimeSlotType());
    }

    @Test
    @DisplayName("TC-ORDER-003: 完成订单 - 添加等候费")
    void testCompleteOrder_WithWaitFee() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(2);
        order.setVehicleType("小面包车");
        order.setTotalAmount(new BigDecimal("100"));
        order.setDriverIncome(new BigDecimal("80"));

        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));
        when(freightConfigService.calculateWaitFee("小面包车", 20)).thenReturn(new BigDecimal("10.00"));
        when(freightConfigService.createWaitFeeDetail(eq(1L), eq("小面包车"), eq(20)))
                .thenReturn(new com.huolala.entity.OrderFeeDetail());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.completeOrder(1L, 20);

        assertEquals(4, result.getStatus());
        assertEquals(20, result.getWaitMinutes());
        assertEquals(new BigDecimal("10.00"), result.getWaitFee());
        assertEquals(new BigDecimal("110.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("88.00"), result.getDriverIncome());
    }

    @Test
    @DisplayName("TC-ORDER-004: 完成订单 - 无等候费")
    void testCompleteOrder_NoWaitFee() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(2);
        order.setTotalAmount(new BigDecimal("100"));
        order.setDriverIncome(new BigDecimal("80"));

        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.completeOrder(1L, null);

        assertEquals(4, result.getStatus());
        assertNull(result.getWaitMinutes());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getWaitFee());
        assertEquals(new BigDecimal("100"), result.getTotalAmount());
    }
}
