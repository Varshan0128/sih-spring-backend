package org.aicte.approval.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aicte.approval.core.model.Application;
import org.aicte.approval.core.model.SlaBreachRecord;
import org.aicte.approval.core.model.SlaRule;
import org.aicte.approval.core.repository.ApplicationRepository;
import org.aicte.approval.core.repository.SlaBreachRecordRepository;
import org.aicte.approval.core.repository.SlaRuleRepository;
import org.aicte.approval.notification.service.EscalationService;
import org.aicte.approval.notification.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlaMonitoringService {

    private final ApplicationRepository applicationRepository;
    private final SlaRuleRepository slaRuleRepository;
    private final SlaBreachRecordRepository breachRecordRepository;
    private final EscalationService escalationService;
    private final NotificationService notificationService;

    @Scheduled(cron = "${app.sla.escalation-check-interval-cron:0 */15 * * * *}")
    @Transactional
    public void monitorSlaDeadlines() {
        Instant now = Instant.now();
        List<Application> activeApplications = applicationRepository.findApproachingOrBreachedApplications(now);

        for (Application app : activeApplications) {
            if (app.getSlaDeadline() != null && app.getSlaDeadline().isBefore(now)) {
                handleBreachedSla(app, now);
            }
        }
    }

    private void handleBreachedSla(Application app, Instant now) {
        if (app.isSlaBreached()) return;

        app.setSlaBreached(true);
        app.setRiskScore(Math.min(100, app.getRiskScore() + 30));
        app.setRiskLevel("HIGH");
        applicationRepository.save(app);

        String stage = app.getCurrentStage().name();
        String approvalCode = app.getApprovalType() != null ? app.getApprovalType().getCode() : null;

        SlaRule rule = slaRuleRepository.findByStageNameAndApprovalTypeCode(stage, approvalCode)
                .or(() -> slaRuleRepository.findFirstByStageNameAndActiveTrue(stage))
                .orElse(null);

        int maxDays = rule != null ? rule.getMaxDurationDays() : 14;
        String escalationRole = rule != null ? rule.getEscalationRole() : "ROLE_COMPETENT_AUTHORITY";

        int elapsedDays = (int) Duration.between(app.getSubmissionDate() != null ?
                app.getSubmissionDate() : app.getCreatedAt(), now).toDays();

        SlaBreachRecord breach = SlaBreachRecord.builder()
                .application(app)
                .stageName(stage)
                .breachTimestamp(now)
                .slaDays(maxDays)
                .elapsedDays(elapsedDays)
                .escalatedToRole(escalationRole)
                .status("ESCALATED")
                .build();
        breachRecordRepository.save(breach);

        // Escalate to authority
        escalationService.escalateCase(
                app,
                "STAGE_" + stage,
                escalationRole,
                String.format("SLA Breached: Stage %s exceeded %d days limit (elapsed: %d days)",
                        stage, maxDays, elapsedDays),
                "AUTO_SLA"
        );

        log.warn("SLA breach processed for Application {}", app.getApplicationNumber());
    }

    @Transactional(readOnly = true)
    public long getRemainingSlaDays(Application app) {
        if (app.getSlaDeadline() == null) return 0;
        Instant now = Instant.now();
        if (app.getSlaDeadline().isBefore(now)) return 0;
        return Duration.between(now, app.getSlaDeadline()).toDays();
    }
}
