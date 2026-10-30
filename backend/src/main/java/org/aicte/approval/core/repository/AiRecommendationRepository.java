package org.aicte.approval.core.repository;

import org.aicte.approval.core.model.AiRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, Long> {
    List<AiRecommendation> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
    List<AiRecommendation> findByApplicationIdAndStatus(Long applicationId, String status);
}
