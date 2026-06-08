package com.huolala.repository;

import com.huolala.entity.DriverLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverLevelRepository extends JpaRepository<DriverLevel, Long> {
    List<DriverLevel> findByStatus(Integer status);
    DriverLevel findByLevelCode(Integer levelCode);
    List<DriverLevel> findAllByOrderByLevelCodeAsc();
}
