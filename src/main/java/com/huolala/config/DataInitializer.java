package com.huolala.config;

import com.huolala.entity.BillingRuleItem;
import com.huolala.entity.CancelRefundRule;
import com.huolala.entity.DriverLevel;
import com.huolala.entity.FreightConfig;
import com.huolala.entity.Region;
import com.huolala.entity.User;
import com.huolala.repository.BillingRuleItemRepository;
import com.huolala.repository.CancelRefundRuleRepository;
import com.huolala.repository.DriverLevelRepository;
import com.huolala.repository.FreightConfigRepository;
import com.huolala.repository.RegionRepository;
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

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private CancelRefundRuleRepository cancelRefundRuleRepository;

    @Autowired
    private BillingRuleItemRepository billingRuleItemRepository;

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

        if (regionRepository.count() == 0) {
            Region bj = new Region();
            bj.setRegionCode("BJ");
            bj.setRegionName("北京");
            bj.setLevel(1);
            bj.setStatus(1);
            regionRepository.save(bj);

            Region sh = new Region();
            sh.setRegionCode("SH");
            sh.setRegionName("上海");
            sh.setLevel(1);
            sh.setStatus(1);
            regionRepository.save(sh);

            Region gz = new Region();
            gz.setRegionCode("GZ");
            gz.setRegionName("广州");
            gz.setLevel(1);
            gz.setStatus(1);
            regionRepository.save(gz);

            Region sz = new Region();
            sz.setRegionCode("SZ");
            sz.setRegionName("深圳");
            sz.setLevel(1);
            sz.setStatus(1);
            regionRepository.save(sz);

            Region cd = new Region();
            cd.setRegionCode("CD");
            cd.setRegionName("成都");
            cd.setLevel(1);
            cd.setStatus(1);
            regionRepository.save(cd);

            System.out.println("初始化区域数据完成");
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

            FreightConfig bjSmall = new FreightConfig();
            bjSmall.setRegionCode("BJ");
            bjSmall.setVehicleType("小面包车");
            bjSmall.setStartPrice(new BigDecimal("40"));
            bjSmall.setStartDistance(new BigDecimal("5"));
            bjSmall.setPricePerKm(new BigDecimal("3.5"));
            bjSmall.setWaitPricePerMin(new BigDecimal("0.6"));
            bjSmall.setCarryPrice(new BigDecimal("60"));
            bjSmall.setFloorSurchargePerFloor(new BigDecimal("6"));
            bjSmall.setPeakTimeStart("07:00");
            bjSmall.setPeakTimeEnd("09:00");
            bjSmall.setPeakSurchargeRate(new BigDecimal("0.20"));
            bjSmall.setNightTimeStart("22:00");
            bjSmall.setNightTimeEnd("06:00");
            bjSmall.setNightSurchargeRate(new BigDecimal("0.25"));
            bjSmall.setMinNightSurcharge(new BigDecimal("12"));
            bjSmall.setDescription("北京区域-小面包车");
            freightConfigRepository.save(bjSmall);

            FreightConfig shSmall = new FreightConfig();
            shSmall.setRegionCode("SH");
            shSmall.setVehicleType("小面包车");
            shSmall.setStartPrice(new BigDecimal("42"));
            shSmall.setStartDistance(new BigDecimal("5"));
            shSmall.setPricePerKm(new BigDecimal("3.8"));
            shSmall.setWaitPricePerMin(new BigDecimal("0.6"));
            shSmall.setCarryPrice(new BigDecimal("60"));
            shSmall.setFloorSurchargePerFloor(new BigDecimal("6"));
            shSmall.setPeakTimeStart("07:30");
            shSmall.setPeakTimeEnd("09:30");
            shSmall.setPeakSurchargeRate(new BigDecimal("0.20"));
            shSmall.setNightTimeStart("22:00");
            shSmall.setNightTimeEnd("06:00");
            shSmall.setNightSurchargeRate(new BigDecimal("0.25"));
            shSmall.setMinNightSurcharge(new BigDecimal("12"));
            shSmall.setDescription("上海区域-小面包车");
            freightConfigRepository.save(shSmall);

            System.out.println("初始化运价配置完成");
        }

        if (cancelRefundRuleRepository.count() == 0) {
            CancelRefundRule rule0 = new CancelRefundRule();
            rule0.setRuleName("待接单取消-全额退款");
            rule0.setFromStatus(0);
            rule0.setToStatus(9);
            rule0.setRefundRate(new BigDecimal("1.0000"));
            rule0.setDescription("司机未接单前取消，全额退款");
            rule0.setSortOrder(1);
            rule0.setStatus(1);
            cancelRefundRuleRepository.save(rule0);

            CancelRefundRule rule1 = new CancelRefundRule();
            rule1.setRuleName("已接单取消-退还80%");
            rule1.setFromStatus(1);
            rule1.setToStatus(9);
            rule1.setRefundRate(new BigDecimal("0.8000"));
            rule1.setDescription("司机已接单但未到达，退还80%");
            rule1.setSortOrder(2);
            rule1.setStatus(1);
            cancelRefundRuleRepository.save(rule1);

            CancelRefundRule rule2 = new CancelRefundRule();
            rule2.setRuleName("已到达取消-退还50%");
            rule2.setFromStatus(2);
            rule2.setToStatus(9);
            rule2.setRefundRate(new BigDecimal("0.5000"));
            rule2.setDescription("司机已到达后取消，退还50%");
            rule2.setSortOrder(3);
            rule2.setStatus(1);
            cancelRefundRuleRepository.save(rule2);

            System.out.println("初始化取消退费规则完成");
        }

        if (billingRuleItemRepository.count() == 0) {
            BillingRuleItem base = new BillingRuleItem();
            base.setRuleCode("BASE");
            base.setRuleName("起步价");
            base.setRuleType("FEE");
            base.setEnabled(1);
            base.setDescription("基础起步价，包含起步里程");
            base.setSortOrder(1);
            base.setStatus(1);
            billingRuleItemRepository.save(base);

            BillingRuleItem mileage = new BillingRuleItem();
            mileage.setRuleCode("MILEAGE");
            mileage.setRuleName("里程费");
            mileage.setRuleType("FEE");
            mileage.setEnabled(1);
            mileage.setDescription("超出起步里程后的按公里计费");
            mileage.setSortOrder(2);
            mileage.setStatus(1);
            billingRuleItemRepository.save(mileage);

            BillingRuleItem peak = new BillingRuleItem();
            peak.setRuleCode("PEAK");
            peak.setRuleName("高峰时段加价");
            peak.setRuleType("SURCHARGE");
            peak.setEnabled(1);
            peak.setDescription("高峰时段按比例加价");
            peak.setSortOrder(3);
            peak.setStatus(1);
            billingRuleItemRepository.save(peak);

            BillingRuleItem night = new BillingRuleItem();
            night.setRuleCode("NIGHT");
            night.setRuleName("夜间服务费");
            night.setRuleType("SURCHARGE");
            night.setEnabled(1);
            night.setDescription("夜间时段加收服务费");
            night.setSortOrder(4);
            night.setStatus(1);
            billingRuleItemRepository.save(night);

            BillingRuleItem floor = new BillingRuleItem();
            floor.setRuleCode("FLOOR");
            floor.setRuleName("楼层费");
            floor.setRuleType("SURCHARGE");
            floor.setEnabled(1);
            floor.setDescription("无电梯楼层搬运费");
            floor.setSortOrder(5);
            floor.setStatus(1);
            billingRuleItemRepository.save(floor);

            BillingRuleItem carry = new BillingRuleItem();
            carry.setRuleCode("CARRY");
            carry.setRuleName("搬运费");
            carry.setRuleType("FEE");
            carry.setEnabled(1);
            carry.setDescription("人工搬运服务费");
            carry.setSortOrder(6);
            carry.setStatus(1);
            billingRuleItemRepository.save(carry);

            BillingRuleItem wait = new BillingRuleItem();
            wait.setRuleCode("WAIT");
            wait.setRuleName("等候费");
            wait.setRuleType("FEE");
            wait.setEnabled(1);
            wait.setDescription("超时等候按分钟计费");
            wait.setSortOrder(7);
            wait.setStatus(1);
            billingRuleItemRepository.save(wait);

            BillingRuleItem other = new BillingRuleItem();
            other.setRuleCode("OTHER");
            other.setRuleName("其他附加费");
            other.setRuleType("SURCHARGE");
            other.setEnabled(1);
            other.setDescription("其他附加服务费用");
            other.setSortOrder(8);
            other.setStatus(1);
            billingRuleItemRepository.save(other);

            System.out.println("初始化计费规则配置完成");
        }
    }
}
