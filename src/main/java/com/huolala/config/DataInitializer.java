package com.huolala.config;

import com.huolala.entity.DriverLevel;
import com.huolala.entity.FreightConfig;
import com.huolala.entity.User;
import com.huolala.repository.DriverLevelRepository;
import com.huolala.repository.FreightConfigRepository;
import com.huolala.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FreightConfigRepository freightConfigRepository;

    @Autowired
    private DriverLevelRepository driverLevelRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("123456");
            admin.setRealName("管理员");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("创建默认管理员账号: admin/123456");
        }

        if (driverLevelRepository.count() == 0) {
            DriverLevel level1 = new DriverLevel();
            level1.setLevelCode(1);
            level1.setLevelName("实习司机");
            level1.setMinOrders(0);
            level1.setMaxOrders(29);
            level1.setCommissionRate(new BigDecimal("0.70"));
            level1.setLevelBonus(BigDecimal.ZERO);
            level1.setDescription("新入职司机，0-29单/月");
            driverLevelRepository.save(level1);

            DriverLevel level2 = new DriverLevel();
            level2.setLevelCode(2);
            level2.setLevelName("银牌司机");
            level2.setMinOrders(30);
            level2.setMaxOrders(49);
            level2.setCommissionRate(new BigDecimal("0.80"));
            level2.setLevelBonus(new BigDecimal("200"));
            level2.setDescription("月完成30-49单，提成80%");
            driverLevelRepository.save(level2);

            DriverLevel level3 = new DriverLevel();
            level3.setLevelCode(3);
            level3.setLevelName("金牌司机");
            level3.setMinOrders(50);
            level3.setMaxOrders(79);
            level3.setCommissionRate(new BigDecimal("0.85"));
            level3.setLevelBonus(new BigDecimal("500"));
            level3.setDescription("月完成50-79单，提成85%");
            driverLevelRepository.save(level3);

            DriverLevel level4 = new DriverLevel();
            level4.setLevelCode(4);
            level4.setLevelName("钻石司机");
            level4.setMinOrders(80);
            level4.setMaxOrders(null);
            level4.setCommissionRate(new BigDecimal("0.90"));
            level4.setLevelBonus(new BigDecimal("1000"));
            level4.setDescription("月完成80单以上，提成90%");
            driverLevelRepository.save(level4);

            System.out.println("初始化司机等级体系完成");
        }

        if (freightConfigRepository.count() == 0) {
            FreightConfig config1 = new FreightConfig();
            config1.setVehicleType("小面包车");
            config1.setStartPrice(new BigDecimal("35"));
            config1.setStartDistance(new BigDecimal("5"));
            config1.setPricePerKm(new BigDecimal("3"));
            config1.setWaitPricePerMin(new BigDecimal("0.5"));
            config1.setCarryPrice(new BigDecimal("50"));
            config1.setFloorSurchargePerFloor(new BigDecimal("5"));
            config1.setPeakTimeStart("07:00");
            config1.setPeakTimeEnd("09:00");
            config1.setPeakSurchargeRate(new BigDecimal("0.15"));
            config1.setNightTimeStart("22:00");
            config1.setNightTimeEnd("06:00");
            config1.setNightSurchargeRate(new BigDecimal("0.20"));
            config1.setMinNightSurcharge(new BigDecimal("10"));
            config1.setDescription("适合小型搬家、少量货物");
            freightConfigRepository.save(config1);

            FreightConfig config2 = new FreightConfig();
            config2.setVehicleType("中面包车");
            config2.setStartPrice(new BigDecimal("55"));
            config2.setStartDistance(new BigDecimal("5"));
            config2.setPricePerKm(new BigDecimal("4"));
            config2.setWaitPricePerMin(new BigDecimal("0.6"));
            config2.setCarryPrice(new BigDecimal("80"));
            config2.setFloorSurchargePerFloor(new BigDecimal("8"));
            config2.setPeakTimeStart("07:00");
            config2.setPeakTimeEnd("09:00");
            config2.setPeakSurchargeRate(new BigDecimal("0.15"));
            config2.setNightTimeStart("22:00");
            config2.setNightTimeEnd("06:00");
            config2.setNightSurchargeRate(new BigDecimal("0.20"));
            config2.setMinNightSurcharge(new BigDecimal("15"));
            config2.setDescription("适合中型搬家、较多货物");
            freightConfigRepository.save(config2);

            FreightConfig config3 = new FreightConfig();
            config3.setVehicleType("小货车");
            config3.setStartPrice(new BigDecimal("85"));
            config3.setStartDistance(new BigDecimal("5"));
            config3.setPricePerKm(new BigDecimal("5"));
            config3.setWaitPricePerMin(new BigDecimal("0.8"));
            config3.setCarryPrice(new BigDecimal("120"));
            config3.setFloorSurchargePerFloor(new BigDecimal("12"));
            config3.setPeakTimeStart("07:00");
            config3.setPeakTimeEnd("09:00");
            config3.setPeakSurchargeRate(new BigDecimal("0.20"));
            config3.setNightTimeStart("22:00");
            config3.setNightTimeEnd("06:00");
            config3.setNightSurchargeRate(new BigDecimal("0.25"));
            config3.setMinNightSurcharge(new BigDecimal("20"));
            config3.setDescription("适合大型搬家、重型货物");
            freightConfigRepository.save(config3);

            FreightConfig config4 = new FreightConfig();
            config4.setVehicleType("中货车");
            config4.setStartPrice(new BigDecimal("120"));
            config4.setStartDistance(new BigDecimal("5"));
            config4.setPricePerKm(new BigDecimal("6"));
            config4.setWaitPricePerMin(new BigDecimal("1.0"));
            config4.setCarryPrice(new BigDecimal("180"));
            config4.setFloorSurchargePerFloor(new BigDecimal("15"));
            config4.setPeakTimeStart("07:00");
            config4.setPeakTimeEnd("09:00");
            config4.setPeakSurchargeRate(new BigDecimal("0.20"));
            config4.setNightTimeStart("22:00");
            config4.setNightTimeEnd("06:00");
            config4.setNightSurchargeRate(new BigDecimal("0.25"));
            config4.setMinNightSurcharge(new BigDecimal("30"));
            config4.setDescription("适合企业搬迁、大宗货物");
            freightConfigRepository.save(config4);

            System.out.println("初始化运价配置完成");
        }
    }
}
