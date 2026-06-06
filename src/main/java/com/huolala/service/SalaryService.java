package com.huolala.service;

import com.huolala.entity.Driver;
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

        BigDecimal baseSalary = driver.getBaseSalary() != null ? driver.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal orderIncome = BigDecimal.ZERO;

        for (Order order : orders) {
            if (order.getDriverIncome() != null) {
                orderIncome = orderIncome.add(order.getDriverIncome());
            }
        }

        BigDecimal bonus = BigDecimal.ZERO;
        if (orderCount >= 50) {
            bonus = new BigDecimal("500");
        } else if (orderCount >= 30) {
            bonus = new BigDecimal("200");
        }

        BigDecimal deduction = BigDecimal.ZERO;

        BigDecimal totalSalary = baseSalary.add(orderIncome).add(bonus).subtract(deduction);

        Salary salary = new Salary();
        salary.setDriver(driver);
        salary.setSalaryMonth(month);
        salary.setOrderCount(orderCount);
        salary.setBaseSalary(baseSalary);
        salary.setOrderIncome(orderIncome);
        salary.setBonus(bonus);
        salary.setDeduction(deduction);
        salary.setTotalSalary(totalSalary.setScale(2, BigDecimal.ROUND_HALF_UP));
        salary.setStatus(1);
        salary.setRemark("系统自动核算");

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
