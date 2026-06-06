package com.huolala.service;

import com.huolala.entity.FreightConfig;
import com.huolala.repository.FreightConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FreightConfigService {
    @Autowired
    private FreightConfigRepository freightConfigRepository;

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

    public BigDecimal calculateFreight(String vehicleType, double distance) {
        FreightConfig config = findByVehicleType(vehicleType);
        if (config == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal freight = config.getStartPrice();
        if (distance > config.getStartDistance().doubleValue()) {
            double extraDistance = distance - config.getStartDistance().doubleValue();
            BigDecimal extraPrice = config.getPricePerKm().multiply(BigDecimal.valueOf(extraDistance));
            freight = freight.add(extraPrice);
        }
        return freight.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal calculateWaitFee(String vehicleType, int waitMinutes) {
        FreightConfig config = findByVehicleType(vehicleType);
        if (config == null) {
            return BigDecimal.ZERO;
        }
        return config.getWaitPricePerMin().multiply(BigDecimal.valueOf(waitMinutes))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
