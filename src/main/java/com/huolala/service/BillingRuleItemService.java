package com.huolala.service;

import com.huolala.entity.BillingRuleItem;
import com.huolala.repository.BillingRuleItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BillingRuleItemService {
    @Autowired
    private BillingRuleItemRepository billingRuleItemRepository;

    public List<BillingRuleItem> findAll() {
        return billingRuleItemRepository.findAllByOrderBySortOrderAsc();
    }

    public List<BillingRuleItem> findActive() {
        return billingRuleItemRepository.findByStatus(1);
    }

    public List<BillingRuleItem> findEnabled() {
        return billingRuleItemRepository.findByEnabledAndStatus(1, 1);
    }

    public BillingRuleItem findById(Long id) {
        return billingRuleItemRepository.findById(id).orElse(null);
    }

    public Optional<BillingRuleItem> findByRuleCode(String ruleCode) {
        return billingRuleItemRepository.findByRuleCode(ruleCode);
    }

    public List<BillingRuleItem> findByRuleType(String ruleType) {
        return billingRuleItemRepository.findByRuleTypeAndStatus(ruleType, 1);
    }

    public BillingRuleItem save(BillingRuleItem item) {
        return billingRuleItemRepository.save(item);
    }

    public void deleteById(Long id) {
        billingRuleItemRepository.deleteById(id);
    }

    public BillingRuleItem toggleEnabled(Long id) {
        BillingRuleItem item = findById(id);
        if (item != null) {
            item.setEnabled(item.getEnabled() == 1 ? 0 : 1);
            return billingRuleItemRepository.save(item);
        }
        return null;
    }
}
