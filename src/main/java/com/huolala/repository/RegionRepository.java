package com.huolala.repository;

import com.huolala.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    List<Region> findByStatus(Integer status);
    Region findByRegionCode(String regionCode);
    List<Region> findByParentCode(String parentCode);
    List<Region> findByLevel(Integer level);
}
