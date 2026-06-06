package com.huolala.service;

import com.huolala.entity.Driver;
import com.huolala.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverService {
    @Autowired
    private DriverRepository driverRepository;

    public List<Driver> findAll() {
        return driverRepository.findAll();
    }

    public List<Driver> findActive() {
        return driverRepository.findByStatus(1);
    }

    public Driver findById(Long id) {
        return driverRepository.findById(id).orElse(null);
    }

    public Driver save(Driver driver) {
        return driverRepository.save(driver);
    }

    public void deleteById(Long id) {
        driverRepository.deleteById(id);
    }

    public Driver findByDriverNo(String driverNo) {
        return driverRepository.findByDriverNo(driverNo);
    }
}
