package com.huolala.service;

import com.huolala.entity.CancelRefundRule;
import com.huolala.repository.CancelRefundRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CancelRefundRuleService {
    @Autowired
    private CancelRefundRuleRepository cancelRefundRuleRepository;

    public List<CancelRefundRule> findAll() {
        return cancelRefundRuleRepository.findAllByOrderBySortOrderAsc();
    }

    public List<CancelRefundRule> findActive() {
        return cancelRefundRuleRepository.findByStatus(1);
    }

    public CancelRefundRule findById(Long id) {
        return cancelRefundRuleRepository.findById(id).orElse(null);
    }

    public CancelRefundRule findByFromStatus(Integer fromStatus) {
        List<CancelRefundRule> rules = cancelRefundRuleRepository.findByFromStatusAndStatus(fromStatus, 1);
        return rules.isEmpty() ? null : rules.get(0);
    }

    public CancelRefundRule save(CancelRefundRule rule) {
        return cancelRefundRuleRepository.save(rule);
    }

    public void deleteById(Long id) {
        cancelRefundRuleRepository.deleteById(id);
    }
}
