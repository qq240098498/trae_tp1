package com.huolala.service;

import com.huolala.entity.Region;
import com.huolala.repository.RegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionService {
    @Autowired
    private RegionRepository regionRepository;

    public List<Region> findAll() {
        return regionRepository.findAll();
    }

    public List<Region> findActive() {
        return regionRepository.findByStatus(1);
    }

    public Region findById(Long id) {
        return regionRepository.findById(id).orElse(null);
    }

    public Region findByRegionCode(String regionCode) {
        return regionRepository.findByRegionCode(regionCode);
    }

    public List<Region> findByParentCode(String parentCode) {
        return regionRepository.findByParentCode(parentCode);
    }

    public List<Region> findByLevel(Integer level) {
        return regionRepository.findByLevel(level);
    }

    public Region save(Region region) {
        return regionRepository.save(region);
    }

    public void deleteById(Long id) {
        regionRepository.deleteById(id);
    }
}
