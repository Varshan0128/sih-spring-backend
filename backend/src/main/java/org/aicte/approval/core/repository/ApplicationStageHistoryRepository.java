package org.aicte.approval.core.repository;

import org.aicte.approval.core.model.ApplicationStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationStageHistoryRepository extends JpaRepository<ApplicationStageHistory, Long> {
    List<ApplicationStageHistory> findByApplicationIdOrderByEnteredAtAsc(Long applicationId);
    Optional<ApplicationStageHistory> findFirstByApplicationIdAndStatusOrderByEnteredAtDesc(Long applicationId, String status);
}
