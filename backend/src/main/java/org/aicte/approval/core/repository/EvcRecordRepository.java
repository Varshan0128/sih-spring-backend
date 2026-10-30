package org.aicte.approval.core.repository;

import org.aicte.approval.core.model.EvcRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvcRecordRepository extends JpaRepository<EvcRecord, Long> {
    Optional<EvcRecord> findByApplicationId(Long applicationId);
    Page<EvcRecord> findByStatus(String status, Pageable pageable);
    Page<EvcRecord> findByCommitteeChairmanId(Long chairmanId, Pageable pageable);
    long countByStatus(String status);
}
