package org.aicte.approval.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aicte.approval.core.model.User;
import org.aicte.approval.core.repository.UserRepository;
import org.aicte.approval.notification.dto.NotificationDtos;
import org.aicte.approval.notification.model.Notification;
import org.aicte.approval.notification.model.NotificationDeliveryAttempt;
import org.aicte.approval.notification.repository.NotificationDeliveryAttemptRepository;
import org.aicte.approval.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryAttemptRepository deliveryAttemptRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification createNotification(Long recipientUserId, String recipientRole,
                                          String title, String message, String type,
                                          String priority, String entityType, Long entityId) {
        // Prevent duplicate alerts for the same event and recipient
        if (entityId != null && recipientUserId != null &&
            notificationRepository.existsByRecipientUserIdAndTitleAndEntityId(recipientUserId, title, entityId)) {
            log.info("Duplicate notification prevented for user {} on entity {}", recipientUserId, entityId);
            return null;
        }

        User recipient = recipientUserId != null ?
                userRepository.findById(recipientUserId).orElse(null) : null;

        Notification notification = Notification.builder()
                .recipientUser(recipient)
                .recipientRole(recipientRole)
                .title(title)
                .message(message)
                .notificationType(type)
                .priority(priority != null ? priority : "MEDIUM")
                .read(false)
                .entityType(entityType)
                .entityId(entityId)
                .createdAt(Instant.now())
                .build();

        Notification saved = notificationRepository.save(notification);

        // Record initial In-App delivery attempt
        NotificationDeliveryAttempt attempt = NotificationDeliveryAttempt.builder()
                .notification(saved)
                .channel("IN_APP")
                .status("SUCCESS")
                .attemptTimestamp(Instant.now())
                .build();
        deliveryAttemptRepository.save(attempt);

        log.info("Notification created [ID: {}]: {}", saved.getId(), title);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<NotificationDtos.NotificationResponseDto> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            n.setReadAt(Instant.now());
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientUserIdAndReadFalse(userId);
        unread.forEach(n -> {
            n.setRead(true);
            n.setReadAt(Instant.now());
        });
        notificationRepository.saveAll(unread);
    }

    public NotificationDtos.NotificationResponseDto mapToDto(Notification n) {
        return NotificationDtos.NotificationResponseDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .notificationType(n.getNotificationType())
                .priority(n.getPriority())
                .isRead(n.isRead())
                .readAt(n.getReadAt())
                .entityType(n.getEntityType())
                .entityId(n.getEntityId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
