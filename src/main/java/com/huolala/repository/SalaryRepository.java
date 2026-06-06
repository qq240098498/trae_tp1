package com.huolala.repository;

import com.huolala.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {
    List<Salary> findByDriverId(Long driverId);
    Salary findByDriverIdAndSalaryMonth(Long driverId, String salaryMonth);
    List<Salary> findBySalaryMonth(String salaryMonth);
}
