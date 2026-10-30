package org.aicte.approval.notification.repository;

import org.aicte.approval.notification.model.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, String entityId);
    Page<AuditEvent> findByActorUsernameOrderByTimestampDesc(String actorUsername, Pageable pageable);
    Optional<AuditEvent> findTopByOrderByIdDesc();
}
