package org.aicte.approval.core.repository;

import org.aicte.approval.core.model.AiRiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiRiskAssessmentRepository extends JpaRepository<AiRiskAssessment, Long> {
    Optional<AiRiskAssessment> findFirstByApplicationIdOrderByAssessmentTimestampDesc(Long applicationId);
}
