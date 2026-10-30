package org.aicte.approval.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aicte.approval.core.model.Application;
import org.aicte.approval.notification.model.EscalationHistory;
import org.aicte.approval.notification.repository.EscalationHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class EscalationService {

    private final EscalationHistoryRepository escalationHistoryRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public EscalationHistory escalateCase(Application application, String fromRole, String toRole, String reason, String triggeredBy) {
        log.warn("Escalating Application {} from {} to {}. Reason: {}",
                application.getApplicationNumber(), fromRole, toRole, reason);

        EscalationHistory escalation = EscalationHistory.builder()
                .application(application)
                .fromRole(fromRole)
                .toRole(toRole)
                .reason(reason)
                .triggeredBy(triggeredBy)
                .escalatedAt(Instant.now())
                .status("ACTIVE")
                .build();

        EscalationHistory saved = escalationHistoryRepository.save(escalation);

        // Notify escalated authority
        notificationService.createNotification(
                null,
                toRole,
                "URGENT ESCALATION: Application " + application.getApplicationNumber(),
                reason + ". Case requires immediate statutory intervention.",
                "ESCALATION",
                "CRITICAL",
                "APPLICATION",
                application.getId()
        );

        // Record immutable audit event
        auditService.recordEvent(
                "SLA_ESCALATION",
                "APPLICATION",
                application.getId().toString(),
                fromRole,
                toRole,
                null
        );

        return saved;
    }
}
