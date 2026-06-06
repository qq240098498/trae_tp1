package com.huolala.repository;

import com.huolala.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByStatus(Integer status);
    Vehicle findByPlateNo(String plateNo);
    List<Vehicle> findByDriverId(Long driverId);
}
