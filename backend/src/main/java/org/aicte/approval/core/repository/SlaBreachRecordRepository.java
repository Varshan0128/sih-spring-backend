package org.aicte.approval.core.repository;

import org.aicte.approval.core.model.SlaBreachRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlaBreachRecordRepository extends JpaRepository<SlaBreachRecord, Long> {
    List<SlaBreachRecord> findByApplicationId(Long applicationId);
    Page<SlaBreachRecord> findByStatus(String status, Pageable pageable);
    long countByStatus(String status);
}
