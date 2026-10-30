package org.aicte.approval.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.aicte.approval.common.dto.ApiResponse;
import org.aicte.approval.common.security.SecurityUtils;
import org.aicte.approval.common.security.UserPrincipal;
import org.aicte.approval.notification.dto.NotificationDtos;
import org.aicte.approval.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications & Reminders", description = "In-app alerts, SLA deadline reminders, and escalation notices")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get notifications for current logged-in user")
    public ResponseEntity<ApiResponse<Page<NotificationDtos.NotificationResponseDto>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        UserPrincipal user = SecurityUtils.getRequiredCurrentUser();
        Page<NotificationDtos.NotificationResponseDto> notifs =
                notificationService.getUserNotifications(user.getId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(notifs));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get count of unread alerts")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        UserPrincipal user = SecurityUtils.getRequiredCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(user.getId())));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark single notification as read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read for current user")
    public ResponseEntity<ApiResponse<String>> markAllAsRead() {
        UserPrincipal user = SecurityUtils.getRequiredCurrentUser();
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }
}
