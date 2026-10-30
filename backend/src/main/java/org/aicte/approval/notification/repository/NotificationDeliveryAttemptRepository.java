package org.aicte.approval.notification.repository;

import org.aicte.approval.notification.model.NotificationDeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationDeliveryAttemptRepository extends JpaRepository<NotificationDeliveryAttempt, Long> {
    List<NotificationDeliveryAttempt> findByNotificationId(Long notificationId);
}
