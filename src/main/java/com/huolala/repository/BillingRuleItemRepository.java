package com.huolala.repository;

import com.huolala.entity.BillingRuleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRuleItemRepository extends JpaRepository<BillingRuleItem, Long> {
    List<BillingRuleItem> findByStatus(Integer status);
    Optional<BillingRuleItem> findByRuleCode(String ruleCode);
    List<BillingRuleItem> findByEnabledAndStatus(Integer enabled, Integer status);
    List<BillingRuleItem> findAllByOrderBySortOrderAsc();
    List<BillingRuleItem> findByRuleTypeAndStatus(String ruleType, Integer status);
}
