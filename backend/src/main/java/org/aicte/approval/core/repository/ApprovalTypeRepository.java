package org.aicte.approval.core.repository;

import org.aicte.approval.core.model.ApprovalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalTypeRepository extends JpaRepository<ApprovalType, Long> {
    Optional<ApprovalType> findByCode(String code);
}
