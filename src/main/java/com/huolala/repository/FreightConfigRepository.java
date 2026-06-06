package com.huolala.repository;

import com.huolala.entity.FreightConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FreightConfigRepository extends JpaRepository<FreightConfig, Long> {
    List<FreightConfig> findByStatus(Integer status);
    FreightConfig findByVehicleType(String vehicleType);
}
