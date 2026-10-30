package org.aicte.approval.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class NotificationDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponseDto {
        private Long id;
        private String title;
        private String message;
        private String notificationType;
        private String priority;
        private boolean isRead;
        private Instant readAt;
        private String entityType;
        private Long entityId;
        private Instant createdAt;
    }
}
