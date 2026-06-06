package com.huolala.service;

import com.huolala.dto.FreightCalculationResult;
import com.huolala.entity.FreightConfig;
import com.huolala.entity.OrderFeeDetail;
import com.huolala.repository.FreightConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FreightConfigServiceTest {

    @Mock
    private FreightConfigRepository freightConfigRepository;

    @InjectMocks
    private FreightConfigService freightConfigService;

    private FreightConfig smallVanConfig;

    @BeforeEach
    void setUp() {
        smallVanConfig = new FreightConfig();
        smallVanConfig.setVehicleType("小面包车");
        smallVanConfig.setStartPrice(new BigDecimal("35"));
        smallVanConfig.setStartDistance(new BigDecimal("5"));
        smallVanConfig.setPricePerKm(new BigDecimal("3"));
        smallVanConfig.setWaitPricePerMin(new BigDecimal("0.5"));
        smallVanConfig.setFloorSurchargePerFloor(new BigDecimal("5"));
        smallVanConfig.setPeakTimeStart("07:00");
        smallVanConfig.setPeakTimeEnd("09:00");
        smallVanConfig.setPeakSurchargeRate(new BigDecimal("0.15"));
        smallVanConfig.setNightTimeStart("22:00");
        smallVanConfig.setNightTimeEnd("06:00");
        smallVanConfig.setNightSurchargeRate(new BigDecimal("0.20"));
        smallVanConfig.setMinNightSurcharge(new BigDecimal("10"));
    }

    @Test
    @DisplayName("TC-FREIGHT-001: 基础运费计算 - 里程在起步价范围内")
    void testCalculateFreight_BaseOnly_WithinStartDistance() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 14, 0);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "小面包车", 3.0, null, orderTime, null, null);

        assertEquals(new BigDecimal("35.00"), result.getBaseFreight());
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getMileageFee());
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getNightSurcharge());
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getFloorSurcharge());
        assertEquals(new BigDecimal("35.00"), result.getTotalAmount());

        List<OrderFeeDetail> details = result.getFeeDetails();
        assertEquals(1, details.size());
        assertEquals("BASE", details.get(0).getFeeType());
    }

    @Test
    @DisplayName("TC-FREIGHT-002: 基础运费计算 - 超出起步里程")
    void testCalculateFreight_WithMileageFee() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 14, 0);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "小面包车", 10.0, null, orderTime, null, null);

        assertEquals(new BigDecimal("35.00"), result.getBaseFreight());
        assertEquals(new BigDecimal("15.00"), result.getMileageFee());
        assertEquals(new BigDecimal("50.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("TC-FREIGHT-003: 夜间加费测试 - 22:00-06:00时段")
    void testCalculateFreight_NightSurcharge() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 23, 30);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "小面包车", 5.0, null, orderTime, null, null);

        assertEquals(new BigDecimal("35.00"), result.getBaseFreight());
        assertEquals(new BigDecimal("7.00"), result.getNightSurcharge());
        assertEquals(new BigDecimal("42.00"), result.getTotalAmount());

        List<OrderFeeDetail> details = result.getFeeDetails();
        assertTrue(details.stream().anyMatch(d -> "NIGHT".equals(d.getFeeType())));
    }

    @Test
    @DisplayName("TC-FREIGHT-004: 夜间加费测试 - 最低夜间服务费")
    void testCalculateFreight_NightSurcharge_Minimum() {
        smallVanConfig.setStartPrice(new BigDecimal("30"));
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 23, 0);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "小面包车", 3.0, null, orderTime, null, null);

        assertEquals(new BigDecimal("30.00"), result.getBaseFreight());
        assertEquals(new BigDecimal("10.00"), result.getNightSurcharge());
    }

    @Test
    @DisplayName("TC-FREIGHT-005: 夜间加费测试 - 凌晨时段03:00")
    void testCalculateFreight_NightSurcharge_EarlyMorning() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 16, 3, 0);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "小面包车", 5.0, null, orderTime, null, null);

        assertEquals(new BigDecimal("7.00"), result.getNightSurcharge());
    }

    @Test
    @DisplayName("TC-FREIGHT-006: 非夜间时段 - 不加收夜间费")
    void testCalculateFreight_NoNightSurcharge_Daytime() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 15, 0);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "小面包车", 5.0, null, orderTime, null, null);

        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getNightSurcharge());
    }

    @Test
    @DisplayName("TC-FREIGHT-007: 楼层费测试 - 5层楼")
    void testCalculateFreight_FloorSurcharge() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 14, 0);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "小面包车", 5.0, 5, orderTime, null, null);

        assertEquals(new BigDecimal("25.00"), result.getFloorSurcharge());
        assertEquals(new BigDecimal("60.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("TC-FREIGHT-008: 楼层费测试 - 0层楼不加收")
    void testCalculateFreight_FloorSurcharge_ZeroFloors() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 14, 0);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "小面包车", 5.0, 0, orderTime, null, null);

        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getFloorSurcharge());
    }

    @Test
    @DisplayName("TC-FREIGHT-009: 楼层费测试 - null楼层数")
    void testCalculateFreight_FloorSurcharge_NullFloors() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 14, 0);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "小面包车", 5.0, null, orderTime, null, null);

        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getFloorSurcharge());
    }

    @Test
    @DisplayName("TC-FREIGHT-010: 搬运费测试")
    void testCalculateFreight_CarryFee() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 14, 0);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "小面包车", 5.0, null, orderTime, new BigDecimal("80"), null);

        assertEquals(new BigDecimal("80.00"), result.getCarryFee());
        assertEquals(new BigDecimal("115.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("TC-FREIGHT-011: 其他附加费测试")
    void testCalculateFreight_OtherSurcharge() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 14, 0);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "小面包车", 5.0, null, orderTime, null, new BigDecimal("50"));

        assertEquals(new BigDecimal("50.00"), result.getOtherSurcharge());
        assertEquals(new BigDecimal("85.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("TC-FREIGHT-012: 综合费用计算 - 夜间+楼层+搬运")
    void testCalculateFreight_Combined_Night_Floor_Carry() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 23, 0);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "小面包车", 10.0, 3, orderTime, new BigDecimal("100"), new BigDecimal("20"));

        assertEquals(new BigDecimal("35.00"), result.getBaseFreight());
        assertEquals(new BigDecimal("15.00"), result.getMileageFee());
        assertEquals(new BigDecimal("10.00"), result.getNightSurcharge());
        assertEquals(new BigDecimal("15.00"), result.getFloorSurcharge());
        assertEquals(new BigDecimal("100.00"), result.getCarryFee());
        assertEquals(new BigDecimal("20.00"), result.getOtherSurcharge());
        assertEquals(new BigDecimal("195.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("TC-FREIGHT-013: 高峰时段加价测试")
    void testCalculateFreight_PeakSurcharge() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 8, 0);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "小面包车", 10.0, null, orderTime, null, null);

        assertEquals(new BigDecimal("7.50"), result.getTimeSlotSurcharge());
        assertEquals(new BigDecimal("57.50"), result.getTotalAmount());
    }

    @Test
    @DisplayName("TC-FREIGHT-014: 夜间时段边界测试 - 22:00整")
    void testCalculateFreight_NightBoundary_2200() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 22, 0);
        String timeSlot = freightConfigService.getTimeSlotType(orderTime, "小面包车");

        assertEquals("NIGHT", timeSlot);
    }

    @Test
    @DisplayName("TC-FREIGHT-015: 夜间时段边界测试 - 06:00整")
    void testCalculateFreight_NightBoundary_0600() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 16, 6, 0);
        String timeSlot = freightConfigService.getTimeSlotType(orderTime, "小面包车");

        assertEquals("NIGHT", timeSlot);
    }

    @Test
    @DisplayName("TC-FREIGHT-016: 夜间时段边界测试 - 21:59")
    void testCalculateFreight_NightBoundary_2159() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 21, 59);
        String timeSlot = freightConfigService.getTimeSlotType(orderTime, "小面包车");

        assertEquals("NORMAL", timeSlot);
    }

    @Test
    @DisplayName("TC-FREIGHT-017: 夜间时段边界测试 - 06:01")
    void testCalculateFreight_NightBoundary_0601() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 16, 6, 1);
        String timeSlot = freightConfigService.getTimeSlotType(orderTime, "小面包车");

        assertEquals("NORMAL", timeSlot);
    }

    @Test
    @DisplayName("TC-FREIGHT-018: 等候费计算")
    void testCalculateWaitFee() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        BigDecimal waitFee = freightConfigService.calculateWaitFee("小面包车", 30);

        assertEquals(new BigDecimal("15.00"), waitFee);
    }

    @Test
    @DisplayName("TC-FREIGHT-019: 车辆类型不存在时返回0")
    void testCalculateFreight_UnknownVehicleType() {
        when(freightConfigRepository.findByVehicleType("大卡车")).thenReturn(null);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 14, 0);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "大卡车", 10.0, null, orderTime, null, null);

        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getTotalAmount());
    }

    @Test
    @DisplayName("TC-FREIGHT-020: 费用明细列表验证")
    void testCalculateFreight_FeeDetailsList() {
        when(freightConfigRepository.findByVehicleType("小面包车")).thenReturn(smallVanConfig);

        LocalDateTime orderTime = LocalDateTime.of(2024, 6, 15, 23, 0);
        FreightCalculationResult result = freightConfigService.calculateFreight(
                "小面包车", 10.0, 2, orderTime, new BigDecimal("50"), new BigDecimal("10"));

        List<OrderFeeDetail> details = result.getFeeDetails();
        assertEquals(6, details.size());

        assertTrue(details.stream().anyMatch(d -> "BASE".equals(d.getFeeType())));
        assertTrue(details.stream().anyMatch(d -> "MILEAGE".equals(d.getFeeType())));
        assertTrue(details.stream().anyMatch(d -> "NIGHT".equals(d.getFeeType())));
        assertTrue(details.stream().anyMatch(d -> "FLOOR".equals(d.getFeeType())));
        assertTrue(details.stream().anyMatch(d -> "CARRY".equals(d.getFeeType())));
        assertTrue(details.stream().anyMatch(d -> "OTHER".equals(d.getFeeType())));
    }
}
