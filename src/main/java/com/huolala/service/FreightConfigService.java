package com.huolala.service;

import com.huolala.dto.FreightCalculationResult;
import com.huolala.entity.BillingRuleItem;
import com.huolala.entity.FreightConfig;
import com.huolala.entity.OrderFeeDetail;
import com.huolala.repository.BillingRuleItemRepository;
import com.huolala.repository.FreightConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FreightConfigService {
    @Autowired
    private FreightConfigRepository freightConfigRepository;

    @Autowired
    private BillingRuleItemRepository billingRuleItemRepository;

    public List<FreightConfig> findAll() {
        return freightConfigRepository.findAll();
    }

    public List<FreightConfig> findActive() {
        return freightConfigRepository.findByStatus(1);
    }

    public FreightConfig findById(Long id) {
        return freightConfigRepository.findById(id).orElse(null);
    }

    public FreightConfig save(FreightConfig config) {
        return freightConfigRepository.save(config);
    }

    public void deleteById(Long id) {
        freightConfigRepository.deleteById(id);
    }

    public FreightConfig findByVehicleType(String vehicleType) {
        return freightConfigRepository.findByVehicleType(vehicleType);
    }

    public FreightConfig findByRegionAndVehicleType(String regionCode, String vehicleType) {
        if (regionCode != null && !regionCode.isEmpty()) {
            FreightConfig regionConfig = freightConfigRepository
                    .findByRegionCodeAndVehicleTypeAndStatus(regionCode, vehicleType, 1);
            if (regionConfig != null) {
                return regionConfig;
            }
        }
        return freightConfigRepository.findByVehicleType(vehicleType);
    }

    public List<FreightConfig> findByRegionCode(String regionCode) {
        if (regionCode != null && !regionCode.isEmpty()) {
            return freightConfigRepository.findByRegionCodeAndStatus(regionCode, 1);
        }
        return freightConfigRepository.findByRegionCodeIsNullAndStatus(1);
    }

    private Map<String, BillingRuleItem> getEnabledBillingRules() {
        List<BillingRuleItem> rules = billingRuleItemRepository.findByEnabledAndStatus(1, 1);
        return rules.stream().collect(Collectors.toMap(BillingRuleItem::getRuleCode, r -> r, (a, b) -> a));
    }

    private boolean isBillingRuleEnabled(Map<String, BillingRuleItem> rules, String ruleCode) {
        BillingRuleItem rule = rules.get(ruleCode);
        return rule != null && rule.getEnabled() == 1;
    }

    public FreightCalculationResult calculateFreight(String vehicleType, double distance,
                                                      Integer floorCount, LocalDateTime orderTime,
                                                      BigDecimal carryFeeInput, BigDecimal otherSurchargeInput) {
        return calculateFreight(null, vehicleType, distance, floorCount, orderTime, carryFeeInput, otherSurchargeInput);
    }

    public FreightCalculationResult calculateFreight(String regionCode, String vehicleType, double distance,
                                                      Integer floorCount, LocalDateTime orderTime,
                                                      BigDecimal carryFeeInput, BigDecimal otherSurchargeInput) {
        FreightConfig config = findByRegionAndVehicleType(regionCode, vehicleType);
        FreightCalculationResult result = new FreightCalculationResult();
        result.setRegionCode(regionCode);

        if (config == null) {
            return result;
        }

        if (orderTime == null) {
            orderTime = LocalDateTime.now();
        }

        Map<String, BillingRuleItem> billingRules = getEnabledBillingRules();

        if (isBillingRuleEnabled(billingRules, "BASE")) {
            BigDecimal baseFreight = config.getStartPrice();
            result.setBaseFreight(baseFreight.setScale(2, BigDecimal.ROUND_HALF_UP));
            addFeeDetail(result.getFeeDetails(), "BASE", "起步价", baseFreight,
                    "包含" + config.getStartDistance() + "公里");
        }

        if (isBillingRuleEnabled(billingRules, "MILEAGE")) {
            BigDecimal mileageFee = BigDecimal.ZERO;
            if (distance > config.getStartDistance().doubleValue()) {
                double extraDistance = distance - config.getStartDistance().doubleValue();
                mileageFee = config.getPricePerKm().multiply(BigDecimal.valueOf(extraDistance));
                result.setMileageFee(mileageFee.setScale(2, BigDecimal.ROUND_HALF_UP));
                addFeeDetail(result.getFeeDetails(), "MILEAGE", "里程费", mileageFee,
                        "超出" + String.format("%.2f", extraDistance) + "公里 × " + config.getPricePerKm() + "元/公里");
            }
        }

        String timeSlotType = determineTimeSlot(orderTime.toLocalTime(), config);

        if (isBillingRuleEnabled(billingRules, "PEAK")) {
            BigDecimal timeSlotSurcharge = BigDecimal.ZERO;
            if ("PEAK".equals(timeSlotType) && config.getPeakSurchargeRate() != null) {
                BigDecimal baseTotal = result.getBaseFreight().add(result.getMileageFee());
                timeSlotSurcharge = baseTotal.multiply(config.getPeakSurchargeRate());
                result.setTimeSlotSurcharge(timeSlotSurcharge.setScale(2, BigDecimal.ROUND_HALF_UP));
                addFeeDetail(result.getFeeDetails(), "PEAK", "高峰时段加价", timeSlotSurcharge,
                        "高峰时段" + config.getPeakTimeStart() + "-" + config.getPeakTimeEnd() + "，加价" + config.getPeakSurchargeRate().multiply(new BigDecimal("100")) + "%");
            }
        }

        if (isBillingRuleEnabled(billingRules, "NIGHT")) {
            BigDecimal nightSurcharge = BigDecimal.ZERO;
            if ("NIGHT".equals(timeSlotType) && config.getNightSurchargeRate() != null) {
                BigDecimal baseTotal = result.getBaseFreight().add(result.getMileageFee());
                nightSurcharge = baseTotal.multiply(config.getNightSurchargeRate());
                if (config.getMinNightSurcharge() != null && nightSurcharge.compareTo(config.getMinNightSurcharge()) < 0) {
                    nightSurcharge = config.getMinNightSurcharge();
                }
                result.setNightSurcharge(nightSurcharge.setScale(2, BigDecimal.ROUND_HALF_UP));
                addFeeDetail(result.getFeeDetails(), "NIGHT", "夜间服务费", nightSurcharge,
                        "夜间时段" + config.getNightTimeStart() + "-" + config.getNightTimeEnd());
            }
        }

        if (isBillingRuleEnabled(billingRules, "FLOOR")) {
            BigDecimal floorSurcharge = BigDecimal.ZERO;
            if (floorCount != null && floorCount > 0 && config.getFloorSurchargePerFloor() != null) {
                floorSurcharge = config.getFloorSurchargePerFloor().multiply(BigDecimal.valueOf(floorCount));
                result.setFloorSurcharge(floorSurcharge.setScale(2, BigDecimal.ROUND_HALF_UP));
                addFeeDetail(result.getFeeDetails(), "FLOOR", "楼层费", floorSurcharge,
                        floorCount + "层 × " + config.getFloorSurchargePerFloor() + "元/层");
            }
        }

        if (isBillingRuleEnabled(billingRules, "CARRY")) {
            BigDecimal carryFee = carryFeeInput != null ? carryFeeInput : BigDecimal.ZERO;
            if (carryFee.compareTo(BigDecimal.ZERO) > 0) {
                result.setCarryFee(carryFee.setScale(2, BigDecimal.ROUND_HALF_UP));
                addFeeDetail(result.getFeeDetails(), "CARRY", "搬运费", carryFee, "人工搬运服务");
            }
        }

        if (isBillingRuleEnabled(billingRules, "OTHER")) {
            BigDecimal otherSurcharge = otherSurchargeInput != null ? otherSurchargeInput : BigDecimal.ZERO;
            if (otherSurcharge.compareTo(BigDecimal.ZERO) > 0) {
                result.setOtherSurcharge(otherSurcharge.setScale(2, BigDecimal.ROUND_HALF_UP));
                addFeeDetail(result.getFeeDetails(), "OTHER", "其他附加费", otherSurcharge, "其他服务费用");
            }
        }

        result.calculateTotal();
        result.setTotalAmount(result.getTotalAmount().setScale(2, BigDecimal.ROUND_HALF_UP));

        return result;
    }

    public BigDecimal calculateWaitFee(String vehicleType, int waitMinutes) {
        return calculateWaitFee(null, vehicleType, waitMinutes);
    }

    public BigDecimal calculateWaitFee(String regionCode, String vehicleType, int waitMinutes) {
        Map<String, BillingRuleItem> billingRules = getEnabledBillingRules();
        if (!isBillingRuleEnabled(billingRules, "WAIT")) {
            return BigDecimal.ZERO;
        }

        FreightConfig config = findByRegionAndVehicleType(regionCode, vehicleType);
        if (config == null) {
            return BigDecimal.ZERO;
        }
        return config.getWaitPricePerMin().multiply(BigDecimal.valueOf(waitMinutes))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public OrderFeeDetail createWaitFeeDetail(Long orderId, String vehicleType, int waitMinutes) {
        return createWaitFeeDetail(null, orderId, vehicleType, waitMinutes);
    }

    public OrderFeeDetail createWaitFeeDetail(String regionCode, Long orderId, String vehicleType, int waitMinutes) {
        FreightConfig config = findByRegionAndVehicleType(regionCode, vehicleType);
        BigDecimal waitFee = calculateWaitFee(regionCode, vehicleType, waitMinutes);

        OrderFeeDetail detail = new OrderFeeDetail();
        detail.setOrderId(orderId);
        detail.setFeeType("WAIT");
        detail.setFeeName("等候费");
        detail.setFeeAmount(waitFee);
        detail.setDescription("等候" + waitMinutes + "分钟 × " + (config != null ? config.getWaitPricePerMin() : "0") + "元/分钟");

        return detail;
    }

    private String determineTimeSlot(LocalTime time, FreightConfig config) {
        if (config.getPeakTimeStart() != null && config.getPeakTimeEnd() != null) {
            LocalTime peakStart = LocalTime.parse(config.getPeakTimeStart());
            LocalTime peakEnd = LocalTime.parse(config.getPeakTimeEnd());
            if (isTimeInRange(time, peakStart, peakEnd)) {
                return "PEAK";
            }
        }

        if (config.getNightTimeStart() != null && config.getNightTimeEnd() != null) {
            LocalTime nightStart = LocalTime.parse(config.getNightTimeStart());
            LocalTime nightEnd = LocalTime.parse(config.getNightTimeEnd());
            if (isTimeInRange(time, nightStart, nightEnd)) {
                return "NIGHT";
            }
        }

        return "NORMAL";
    }

    private boolean isTimeInRange(LocalTime time, LocalTime start, LocalTime end) {
        if (start.isBefore(end)) {
            return !time.isBefore(start) && !time.isAfter(end);
        } else {
            return !time.isBefore(start) || !time.isAfter(end);
        }
    }

    private void addFeeDetail(List<OrderFeeDetail> details, String feeType, String feeName,
                              BigDecimal amount, String description) {
        OrderFeeDetail detail = new OrderFeeDetail();
        detail.setFeeType(feeType);
        detail.setFeeName(feeName);
        detail.setFeeAmount(amount.setScale(2, BigDecimal.ROUND_HALF_UP));
        detail.setDescription(description);
        details.add(detail);
    }

    public String getTimeSlotType(LocalDateTime orderTime, String vehicleType) {
        return getTimeSlotType(null, orderTime, vehicleType);
    }

    public String getTimeSlotType(String regionCode, LocalDateTime orderTime, String vehicleType) {
        FreightConfig config = findByRegionAndVehicleType(regionCode, vehicleType);
        if (config == null) {
            return "NORMAL";
        }
        return determineTimeSlot(orderTime.toLocalTime(), config);
    }
}
