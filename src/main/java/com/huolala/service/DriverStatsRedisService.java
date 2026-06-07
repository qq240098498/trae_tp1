package com.huolala.service;

import com.huolala.dto.DriverMileageStats;
import com.huolala.entity.Driver;
import com.huolala.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class DriverStatsRedisService {

    private static final String MILEAGE_RANK_KEY = "driver:mileage:rank:";
    private static final String ORDER_COUNT_RANK_KEY = "driver:order:count:rank:";
    private static final String INCOME_RANK_KEY = "driver:income:rank:";
    private static final String FREIGHT_RANK_KEY = "driver:freight:rank:";
    private static final String STATS_DETAIL_KEY = "driver:stats:detail:";
    private static final long CACHE_EXPIRE_HOURS = 24;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DriverService driverService;

    private String getMonthKey(String month) {
        return month;
    }

    public void updateDriverStats(Order order) {
        if (order.getDriver() == null || order.getCompleteTime() == null) {
            return;
        }

        String month = order.getCompleteTime().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Long driverId = order.getDriver().getId();
        String driverIdStr = driverId.toString();
        String monthKey = getMonthKey(month);

        Double distance = order.getDistance() != null ? order.getDistance() : 0.0;
        redisTemplate.opsForZSet().incrementScore(MILEAGE_RANK_KEY + monthKey, driverIdStr, distance);

        redisTemplate.opsForZSet().incrementScore(ORDER_COUNT_RANK_KEY + monthKey, driverIdStr, 1);

        BigDecimal driverIncome = order.getDriverIncome() != null ? order.getDriverIncome() : BigDecimal.ZERO;
        redisTemplate.opsForZSet().incrementScore(INCOME_RANK_KEY + monthKey, driverIdStr, driverIncome.doubleValue());

        BigDecimal totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        redisTemplate.opsForZSet().incrementScore(FREIGHT_RANK_KEY + monthKey, driverIdStr, totalAmount.doubleValue());

        updateStatsDetail(driverId, month, order);

        setKeyExpire(MILEAGE_RANK_KEY + monthKey);
        setKeyExpire(ORDER_COUNT_RANK_KEY + monthKey);
        setKeyExpire(INCOME_RANK_KEY + monthKey);
        setKeyExpire(FREIGHT_RANK_KEY + monthKey);
        setKeyExpire(STATS_DETAIL_KEY + monthKey);
    }

    @SuppressWarnings("unchecked")
    private void updateStatsDetail(Long driverId, String month, Order order) {
        String monthKey = getMonthKey(month);
        String hashKey = driverId.toString();

        Map<String, Object> stats = (Map<String, Object>) redisTemplate.opsForHash().get(STATS_DETAIL_KEY + monthKey, hashKey);
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("driverId", driverId);
            stats.put("totalMileage", 0.0);
            stats.put("orderCount", 0);
            stats.put("totalIncome", 0.0);
            stats.put("totalFreight", 0.0);
        }

        Double currentMileage = ((Number) stats.get("totalMileage")).doubleValue();
        Integer currentOrderCount = ((Number) stats.get("orderCount")).intValue();
        Double currentIncome = ((Number) stats.get("totalIncome")).doubleValue();
        Double currentFreight = ((Number) stats.get("totalFreight")).doubleValue();

        Double distance = order.getDistance() != null ? order.getDistance() : 0.0;
        BigDecimal driverIncome = order.getDriverIncome() != null ? order.getDriverIncome() : BigDecimal.ZERO;
        BigDecimal totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

        stats.put("totalMileage", currentMileage + distance);
        stats.put("orderCount", currentOrderCount + 1);
        stats.put("totalIncome", currentIncome + driverIncome.doubleValue());
        stats.put("totalFreight", currentFreight + totalAmount.doubleValue());

        redisTemplate.opsForHash().put(STATS_DETAIL_KEY + monthKey, hashKey, stats);
    }

    @SuppressWarnings("unchecked")
    public List<DriverMileageStats> getDriverMileageStatsFromRedis(String month, String sortBy, String sortOrder) {
        String monthKey = getMonthKey(month);
        String rankKey;

        switch (sortBy) {
            case "orderCount":
                rankKey = ORDER_COUNT_RANK_KEY + monthKey;
                break;
            case "totalIncome":
                rankKey = INCOME_RANK_KEY + monthKey;
                break;
            case "totalFreight":
                rankKey = FREIGHT_RANK_KEY + monthKey;
                break;
            case "avgMileagePerOrder":
            case "totalMileage":
            default:
                rankKey = MILEAGE_RANK_KEY + monthKey;
                break;
        }

        boolean ascending = "asc".equalsIgnoreCase(sortOrder);
        Set<Object> driverIds;

        if (ascending) {
            driverIds = redisTemplate.opsForZSet().range(rankKey, 0, -1);
        } else {
            driverIds = redisTemplate.opsForZSet().reverseRange(rankKey, 0, -1);
        }

        if (driverIds == null || driverIds.isEmpty()) {
            return null;
        }

        List<Driver> allDrivers = driverService.findAll();
        Map<Long, Driver> driverMap = new HashMap<>();
        for (Driver driver : allDrivers) {
            driverMap.put(driver.getId(), driver);
        }

        List<DriverMileageStats> statsList = new ArrayList<>();
        String detailKey = STATS_DETAIL_KEY + monthKey;

        for (Object driverIdObj : driverIds) {
            String driverIdStr = driverIdObj.toString();
            Long driverId = Long.parseLong(driverIdStr);

            Map<String, Object> detailMap = (Map<String, Object>) redisTemplate.opsForHash().get(detailKey, driverIdStr);
            Driver driver = driverMap.get(driverId);

            if (detailMap == null || driver == null) {
                continue;
            }

            DriverMileageStats stats = new DriverMileageStats();
            stats.setDriverId(driverId);
            stats.setDriverName(driver.getName());
            stats.setDriverNo(driver.getDriverNo());

            Double totalMileage = ((Number) detailMap.get("totalMileage")).doubleValue();
            Integer orderCount = ((Number) detailMap.get("orderCount")).intValue();
            Double totalIncome = ((Number) detailMap.get("totalIncome")).doubleValue();
            Double totalFreight = ((Number) detailMap.get("totalFreight")).doubleValue();

            stats.setTotalMileage(Math.round(totalMileage * 100.0) / 100.0);
            stats.setOrderCount(orderCount);
            stats.setTotalIncome(BigDecimal.valueOf(totalIncome).setScale(2, BigDecimal.ROUND_HALF_UP));
            stats.setTotalFreight(BigDecimal.valueOf(totalFreight).setScale(2, BigDecimal.ROUND_HALF_UP));

            double avgMileage = orderCount > 0 ? totalMileage / orderCount : 0;
            stats.setAvgMileagePerOrder(Math.round(avgMileage * 100.0) / 100.0);

            statsList.add(stats);
        }

        if ("avgMileagePerOrder".equals(sortBy)) {
            if (ascending) {
                statsList.sort(Comparator.comparingDouble(DriverMileageStats::getAvgMileagePerOrder));
            } else {
                statsList.sort((s1, s2) -> Double.compare(s2.getAvgMileagePerOrder(), s1.getAvgMileagePerOrder()));
            }
        }

        return statsList.isEmpty() ? null : statsList;
    }

    public void rebuildStats(String month, List<DriverMileageStats> statsList) {
        clearMonthStats(month);
        String monthKey = getMonthKey(month);

        for (DriverMileageStats stats : statsList) {
            String driverIdStr = stats.getDriverId().toString();

            if (stats.getTotalMileage() != null) {
                redisTemplate.opsForZSet().add(MILEAGE_RANK_KEY + monthKey, driverIdStr, stats.getTotalMileage());
            }
            if (stats.getOrderCount() != null) {
                redisTemplate.opsForZSet().add(ORDER_COUNT_RANK_KEY + monthKey, driverIdStr, stats.getOrderCount());
            }
            if (stats.getTotalIncome() != null) {
                redisTemplate.opsForZSet().add(INCOME_RANK_KEY + monthKey, driverIdStr, stats.getTotalIncome().doubleValue());
            }
            if (stats.getTotalFreight() != null) {
                redisTemplate.opsForZSet().add(FREIGHT_RANK_KEY + monthKey, driverIdStr, stats.getTotalFreight().doubleValue());
            }

            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("driverId", stats.getDriverId());
            detailMap.put("totalMileage", stats.getTotalMileage() != null ? stats.getTotalMileage() : 0.0);
            detailMap.put("orderCount", stats.getOrderCount() != null ? stats.getOrderCount() : 0);
            detailMap.put("totalIncome", stats.getTotalIncome() != null ? stats.getTotalIncome().doubleValue() : 0.0);
            detailMap.put("totalFreight", stats.getTotalFreight() != null ? stats.getTotalFreight().doubleValue() : 0.0);

            redisTemplate.opsForHash().put(STATS_DETAIL_KEY + monthKey, driverIdStr, detailMap);
        }

        setKeyExpire(MILEAGE_RANK_KEY + monthKey);
        setKeyExpire(ORDER_COUNT_RANK_KEY + monthKey);
        setKeyExpire(INCOME_RANK_KEY + monthKey);
        setKeyExpire(FREIGHT_RANK_KEY + monthKey);
        setKeyExpire(STATS_DETAIL_KEY + monthKey);
    }

    public void clearMonthStats(String month) {
        String monthKey = getMonthKey(month);
        redisTemplate.delete(MILEAGE_RANK_KEY + monthKey);
        redisTemplate.delete(ORDER_COUNT_RANK_KEY + monthKey);
        redisTemplate.delete(INCOME_RANK_KEY + monthKey);
        redisTemplate.delete(FREIGHT_RANK_KEY + monthKey);
        redisTemplate.delete(STATS_DETAIL_KEY + monthKey);
    }

    public boolean hasMonthStats(String month) {
        String monthKey = getMonthKey(month);
        Long size = redisTemplate.opsForZSet().size(MILEAGE_RANK_KEY + monthKey);
        return size != null && size > 0;
    }

    private void setKeyExpire(String key) {
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return;
        }
        redisTemplate.expire(key, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
    }
}
