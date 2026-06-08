package com.huolala.repository;

import com.huolala.entity.FreightConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FreightConfigRepository extends JpaRepository<FreightConfig, Long> {
    List<FreightConfig> findByStatus(Integer status);
    FreightConfig findByVehicleType(String vehicleType);
    FreightConfig findByRegionCodeAndVehicleTypeAndStatus(String regionCode, String vehicleType, Integer status);
    List<FreightConfig> findByRegionCodeAndStatus(String regionCode, Integer status);
    List<FreightConfig> findByRegionCodeIsNullAndStatus(Integer status);
}
