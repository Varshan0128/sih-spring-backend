package org.aicte.approval.notification.repository;

import org.aicte.approval.notification.model.EscalationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalationHistoryRepository extends JpaRepository<EscalationHistory, Long> {
    List<EscalationHistory> findByApplicationIdOrderByEscalatedAtDesc(Long applicationId);
    Page<EscalationHistory> findByStatus(String status, Pageable pageable);
    long countByStatus(String status);
}
