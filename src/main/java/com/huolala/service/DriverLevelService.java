package com.huolala.service;

import com.huolala.entity.DriverLevel;
import com.huolala.repository.DriverLevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DriverLevelService {
    @Autowired
    private DriverLevelRepository driverLevelRepository;

    public List<DriverLevel> findAll() {
        return driverLevelRepository.findAllByOrderByLevelCodeAsc();
    }

    public List<DriverLevel> findActive() {
        return driverLevelRepository.findByStatus(1);
    }

    public DriverLevel findById(Long id) {
        return driverLevelRepository.findById(id).orElse(null);
    }

    public DriverLevel findByLevelCode(Integer levelCode) {
        return driverLevelRepository.findByLevelCode(levelCode);
    }

    public DriverLevel save(DriverLevel driverLevel) {
        return driverLevelRepository.save(driverLevel);
    }

    public void deleteById(Long id) {
        driverLevelRepository.deleteById(id);
    }

    public DriverLevel determineLevelByOrderCount(Integer orderCount) {
        if (orderCount == null || orderCount < 0) {
            orderCount = 0;
        }
        List<DriverLevel> levels = driverLevelRepository.findAllByOrderByLevelCodeAsc();
        DriverLevel matchedLevel = null;
        for (DriverLevel level : levels) {
            if (level.getStatus() != null && level.getStatus() != 1) {
                continue;
            }
            if (orderCount >= level.getMinOrders()) {
                matchedLevel = level;
            }
        }
        return matchedLevel;
    }

    public BigDecimal getCommissionRateByLevelCode(Integer levelCode) {
        if (levelCode == null) {
            return new BigDecimal("0.70");
        }
        DriverLevel level = findByLevelCode(levelCode);
        if (level != null && level.getCommissionRate() != null) {
            return level.getCommissionRate();
        }
        return new BigDecimal("0.70");
    }
}
