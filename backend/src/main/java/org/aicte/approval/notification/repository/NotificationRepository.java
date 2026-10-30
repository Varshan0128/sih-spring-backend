package org.aicte.approval.notification.repository;

import org.aicte.approval.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId, Pageable pageable);
    Page<Notification> findByRecipientRoleOrderByCreatedAtDesc(String recipientRole, Pageable pageable);
    List<Notification> findByRecipientUserIdAndReadFalse(Long recipientUserId);
    long countByRecipientUserIdAndReadFalse(Long recipientUserId);
    boolean existsByRecipientUserIdAndTitleAndEntityId(Long recipientUserId, String title, Long entityId);
}
