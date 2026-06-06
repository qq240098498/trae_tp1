package com.huolala.config;

import com.huolala.entity.FreightConfig;
import com.huolala.entity.User;
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
