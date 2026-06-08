package com.huolala.service;

import com.huolala.entity.Driver;
import com.huolala.entity.DriverLevel;
import com.huolala.entity.Order;
import com.huolala.entity.Salary;
import com.huolala.repository.SalaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SalaryService {
    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private DriverService driverService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private DriverLevelService driverLevelService;

    public List<Salary> findAll() {
        return salaryRepository.findAll();
    }

    public Salary findById(Long id) {
        return salaryRepository.findById(id).orElse(null);
    }

    public List<Salary> findByDriverId(Long driverId) {
        return salaryRepository.findByDriverId(driverId);
    }

    public Salary findByDriverIdAndMonth(Long driverId, String month) {
        return salaryRepository.findByDriverIdAndSalaryMonth(driverId, month);
    }

    public List<Salary> findByMonth(String month) {
        return salaryRepository.findBySalaryMonth(month);
    }

    @Transactional
    public Salary calculateSalary(Long driverId, String month) {
        Driver driver = driverService.findById(driverId);
        if (driver == null) {
            throw new RuntimeException("司机不存在");
        }

        Salary existSalary = findByDriverIdAndMonth(driverId, month);
        if (existSalary != null) {
            return existSalary;
        }

        List<Order> orders = orderService.findCompletedOrdersByDriverAndMonth(driverId, month);
        Integer orderCount = orderService.countCompletedOrdersByDriverAndMonth(driverId, month);

        DriverLevel driverLevel = driverLevelService.determineLevelByOrderCount(orderCount);

        BigDecimal commissionRate;
        String levelName;
        BigDecimal levelBonus;
        if (driverLevel != null) {
            commissionRate = driverLevel.getCommissionRate();
            levelName = driverLevel.getLevelName();
            levelBonus = driverLevel.getLevelBonus() != null ? driverLevel.getLevelBonus() : BigDecimal.ZERO;
        } else {
            commissionRate = new BigDecimal("0.70");
            levelName = "实习司机";
            levelBonus = BigDecimal.ZERO;
        }

        if (driver.getLevelCode() != null && driverLevel != null) {
            DriverLevel assignedLevel = driverLevelService.findByLevelCode(driver.getLevelCode());
            if (assignedLevel != null && assignedLevel.getStatus() != null && assignedLevel.getStatus() == 1) {
                commissionRate = assignedLevel.getCommissionRate();
                levelName = assignedLevel.getLevelName();
                levelBonus = assignedLevel.getLevelBonus() != null ? assignedLevel.getLevelBonus() : BigDecimal.ZERO;
            }
        }

        BigDecimal baseSalary = driver.getBaseSalary() != null ? driver.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getTotalAmount() != null) {
                totalOrderAmount = totalOrderAmount.add(order.getTotalAmount());
            }
        }

        BigDecimal orderIncome = totalOrderAmount.multiply(commissionRate).setScale(2, BigDecimal.ROUND_HALF_UP);

        BigDecimal bonus = BigDecimal.ZERO;
        if (orderCount >= 80) {
            bonus = new BigDecimal("1000");
        } else if (orderCount >= 50) {
            bonus = new BigDecimal("500");
        } else if (orderCount >= 30) {
            bonus = new BigDecimal("200");
        }

        BigDecimal deduction = BigDecimal.ZERO;

        BigDecimal totalSalary = baseSalary.add(orderIncome).add(levelBonus).add(bonus).subtract(deduction);

        Salary salary = new Salary();
        salary.setDriver(driver);
        salary.setSalaryMonth(month);
        salary.setOrderCount(orderCount);
        salary.setLevelName(levelName);
        salary.setCommissionRate(commissionRate);
        salary.setBaseSalary(baseSalary);
        salary.setOrderIncome(orderIncome);
        salary.setLevelBonus(levelBonus);
        salary.setBonus(bonus);
        salary.setDeduction(deduction);
        salary.setTotalSalary(totalSalary.setScale(2, BigDecimal.ROUND_HALF_UP));
        salary.setStatus(1);
        salary.setRemark("系统自动核算-等级:" + levelName + " 提成率:" + commissionRate.multiply(new BigDecimal("100")).setScale(0) + "%");

        return salaryRepository.save(salary);
    }

    @Transactional
    public void batchCalculateSalary(String month) {
        List<Driver> drivers = driverService.findAll();
        for (Driver driver : drivers) {
            try {
                calculateSalary(driver.getId(), month);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Salary save(Salary salary) {
        return salaryRepository.save(salary);
    }

    public void deleteById(Long id) {
        salaryRepository.deleteById(id);
    }
}
