package org.aicte.approval.core.repository;

import org.aicte.approval.core.model.ComplianceRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceRequirementRepository extends JpaRepository<ComplianceRequirement, Long> {
    List<ComplianceRequirement> findByApplicationId(Long applicationId);
    long countByApplicationIdAndStatus(Long applicationId, String status);
}
