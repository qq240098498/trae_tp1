package com.huolala.config;

import com.huolala.dto.DriverMileageStats;
import com.huolala.service.DriverStatsRedisService;
import com.huolala.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.redis.warmup", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisWarmupConfig implements ApplicationRunner {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DriverStatsRedisService driverStatsRedisService;

    @Autowired
    private OrderService orderService;

    @Value("${app.env:dev}")
    private String env;

    private static final String WARMUP_KEY = "app:warmup:done";
    private static final long WARMUP_EXPIRE_HOURS = 24;

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始 Redis 预热，当前环境: {}", env);
        long startTime = System.currentTimeMillis();

        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(WARMUP_KEY))) {
                log.info("Redis 已预热过，跳过预热流程");
                return;
            }

            pingRedis();
            preloadDriverStats();
            preloadCommonData();

            redisTemplate.opsForValue().set(WARMUP_KEY, true, WARMUP_EXPIRE_HOURS, TimeUnit.HOURS);

            long costTime = System.currentTimeMillis() - startTime;
            log.info("Redis 预热完成，耗时: {}ms", costTime);

        } catch (Exception e) {
            log.error("Redis 预热失败，继续启动应用", e);
        }
    }

    private void pingRedis() {
        try {
            String result = redisTemplate.getConnectionFactory().getConnection().ping();
            log.info("Redis 连接测试: {}", result);
        } catch (Exception e) {
            log.error("Redis 连接测试失败", e);
            throw e;
        }
    }

    private void preloadDriverStats() {
        try {
            String currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            log.info("预热司机统计数据，月份: {}", currentMonth);

            if (!driverStatsRedisService.hasMonthStats(currentMonth)) {
                List<DriverMileageStats> stats = orderService.getDriverMileageStats(currentMonth, "totalMileage", "desc");
                log.info("预热司机统计数据完成，共 {} 条记录", stats.size());
            } else {
                log.info("司机统计数据已存在，跳过预热");
            }

            String lastMonth = YearMonth.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            log.info("预热上月司机统计数据，月份: {}", lastMonth);
            if (!driverStatsRedisService.hasMonthStats(lastMonth)) {
                List<DriverMileageStats> stats = orderService.getDriverMileageStats(lastMonth, "totalMileage", "desc");
                log.info("预热上月司机统计数据完成，共 {} 条记录", stats.size());
            } else {
                log.info("上月司机统计数据已存在，跳过预热");
            }
        } catch (Exception e) {
            log.error("预热司机统计数据失败", e);
        }
    }

    private void preloadCommonData() {
        try {
            log.info("预热常用数据缓存...");
            String appInfoKey = "app:info";
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(appInfoKey))) {
                redisTemplate.opsForValue().set(appInfoKey, "huolala-management-system", 1, TimeUnit.HOURS);
            }
            log.info("常用数据缓存预热完成");
        } catch (Exception e) {
            log.error("预热常用数据缓存失败", e);
        }
    }
}
