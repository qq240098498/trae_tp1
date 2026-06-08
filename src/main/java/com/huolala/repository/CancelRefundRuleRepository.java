package com.huolala.repository;

import com.huolala.entity.CancelRefundRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CancelRefundRuleRepository extends JpaRepository<CancelRefundRule, Long> {
    List<CancelRefundRule> findByStatus(Integer status);
    List<CancelRefundRule> findByFromStatusAndStatus(Integer fromStatus, Integer status);
    CancelRefundRule findByFromStatusAndToStatusAndStatus(Integer fromStatus, Integer toStatus, Integer status);
    List<CancelRefundRule> findAllByOrderBySortOrderAsc();
}
