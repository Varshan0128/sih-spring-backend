package org.aicte.approval.core.repository;

import org.aicte.approval.core.model.SlaRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaRuleRepository extends JpaRepository<SlaRule, Long> {
    Optional<SlaRule> findByStageNameAndApprovalTypeCode(String stageName, String approvalTypeCode);
    Optional<SlaRule> findFirstByStageNameAndActiveTrue(String stageName);
}
