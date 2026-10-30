package org.aicte.approval.core.repository;

import org.aicte.approval.core.model.WorkflowTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long> {
    List<WorkflowTask> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
    Page<WorkflowTask> findByAssignedToUserIdAndStatus(Long userId, String status, Pageable pageable);
    Page<WorkflowTask> findByAssignedRoleAndStatus(String assignedRole, String status, Pageable pageable);
    long countByAssignedToUserIdAndStatus(Long userId, String status);
    long countByAssignedRoleAndStatus(String assignedRole, String status);
}
