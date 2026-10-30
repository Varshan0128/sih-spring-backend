package org.aicte.approval.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aicte.approval.common.exception.InvalidWorkflowActionException;
import org.aicte.approval.common.exception.UnauthorizedAccessException;
import org.aicte.approval.common.security.RoleConstants;
import org.aicte.approval.common.security.SecurityUtils;
import org.aicte.approval.common.security.UserPrincipal;
import org.aicte.approval.core.model.*;
import org.aicte.approval.core.repository.*;
import org.aicte.approval.notification.service.AuditService;
import org.aicte.approval.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngineService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStageHistoryRepository stageHistoryRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final SlaRuleRepository slaRuleRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    // Permitted Stage Transitions State Machine
    private static final Map<ApplicationStage, Set<ApplicationStage>> PERMITTED_TRANSITIONS = Map.of(
            ApplicationStage.DRAFT, Set.of(ApplicationStage.SUBMITTED, ApplicationStage.WITHDRAWN),
            ApplicationStage.SUBMITTED, Set.of(ApplicationStage.SCRUTINY, ApplicationStage.WITHDRAWN),
            ApplicationStage.SCRUTINY, Set.of(
                    ApplicationStage.DEFICIENCY_COMMUNICATED,
                    ApplicationStage.EVC_PENDING,
                    ApplicationStage.SAC_REVIEW,
                    ApplicationStage.APPROVAL_PENDING,
                    ApplicationStage.REJECTED
            ),
            ApplicationStage.DEFICIENCY_COMMUNICATED, Set.of(ApplicationStage.SCRUTINY, ApplicationStage.WITHDRAWN),
            ApplicationStage.EVC_PENDING, Set.of(ApplicationStage.EVC_SCHEDULED),
            ApplicationStage.EVC_SCHEDULED, Set.of(ApplicationStage.EVC_COMPLETED),
            ApplicationStage.EVC_COMPLETED, Set.of(ApplicationStage.SAC_REVIEW, ApplicationStage.APPROVAL_PENDING, ApplicationStage.REJECTED),
            ApplicationStage.SAC_REVIEW, Set.of(ApplicationStage.APPROVAL_PENDING, ApplicationStage.REJECTED),
            ApplicationStage.APPROVAL_PENDING, Set.of(ApplicationStage.APPROVED, ApplicationStage.REJECTED)
    );

    @Transactional
    public Application transitionStage(Long applicationId, ApplicationStage targetStage, String remarks, String action) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new InvalidWorkflowActionException("Application not found with id: " + applicationId));

        UserPrincipal currentUser = SecurityUtils.getRequiredCurrentUser();
        ApplicationStage currentStage = application.getCurrentStage();

        // 1. Validate State Transition Validity
        Set<ApplicationStage> allowedNextStages = PERMITTED_TRANSITIONS.get(currentStage);
        if (allowedNextStages == null || !allowedNextStages.contains(targetStage)) {
            throw new InvalidWorkflowActionException(String.format(
                    "Illegal statutory transition: Cannot move Application from stage '%s' to '%s'.",
                    currentStage, targetStage));
        }

        // 2. Validate Statutory Authority & Role Check
        validateAuthorityForTransition(currentStage, targetStage, currentUser);

        Instant now = Instant.now();

        // 3. Close Active Stage in History
        stageHistoryRepository.findFirstByApplicationIdAndStatusOrderByEnteredAtDesc(applicationId, "IN_PROGRESS")
                .ifPresent(activeStage -> {
                    activeStage.setExitedAt(now);
                    int hours = (int) Duration.between(activeStage.getEnteredAt(), now).toHours();
                    activeStage.setDurationHours(hours);
                    activeStage.setStatus("COMPLETED");
                    activeStage.setRemarks(remarks);
                    userRepository.findById(currentUser.getId()).ifPresent(activeStage::setCompletedByUser);
                    stageHistoryRepository.save(activeStage);
                });

        // 4. Update Application State
        application.setCurrentStage(targetStage);
        if (targetStage == ApplicationStage.APPROVED) {
            application.setStatus("APPROVED");
        } else if (targetStage == ApplicationStage.REJECTED) {
            application.setStatus("REJECTED");
        } else if (targetStage == ApplicationStage.WITHDRAWN) {
            application.setStatus("WITHDRAWN");
        } else {
            application.setStatus("IN_PROGRESS");
        }

        // 5. Calculate New Stage SLA
        updateStageSla(application, targetStage, now);

        Application updatedApplication = applicationRepository.save(application);

        // 6. Open New Stage History Entry
        ApplicationStageHistory newStageHistory = ApplicationStageHistory.builder()
                .application(updatedApplication)
                .stageName(targetStage.name())
                .enteredAt(now)
                .status("IN_PROGRESS")
                .remarks("Transitioned from " + currentStage.name() + " with action: " + action)
                .build();
        stageHistoryRepository.save(newStageHistory);

        // 7. Create Workflow Tasks & Assign Responsible Officers
        createTasksForNewStage(updatedApplication, targetStage);

        // 8. Record Immutable Audit Event
        auditService.recordEvent(
                "STAGE_TRANSITION_" + targetStage.name(),
                "APPLICATION",
                application.getId().toString(),
                currentStage.name(),
                targetStage.name(),
                null
        );

        log.info("Application {} transitioned from {} to {}", application.getApplicationNumber(), currentStage, targetStage);
        return updatedApplication;
    }

    private void validateAuthorityForTransition(ApplicationStage current, ApplicationStage target, UserPrincipal user) {
        boolean isSystemAdmin = SecurityUtils.isSystemAdmin();
        if (isSystemAdmin) return; // Admin can override if permitted

        if (target == ApplicationStage.APPROVED || target == ApplicationStage.REJECTED) {
            if (!SecurityUtils.hasRole(RoleConstants.ROLE_COMPETENT_AUTHORITY)) {
                throw new UnauthorizedAccessException("Statutory restriction: Only the Competent Authority can Approve or Reject applications.");
            }
        } else if (current == ApplicationStage.DRAFT && target == ApplicationStage.SUBMITTED) {
            if (!SecurityUtils.hasRole(RoleConstants.ROLE_INSTITUTION)) {
                throw new UnauthorizedAccessException("Only institution users can submit an application.");
            }
        } else if (current == ApplicationStage.SCRUTINY) {
            if (!SecurityUtils.hasRole(RoleConstants.ROLE_SCRUTINY_COMMITTEE) &&
                !SecurityUtils.hasRole(RoleConstants.ROLE_BUREAU_OFFICER) &&
                !SecurityUtils.hasRole(RoleConstants.ROLE_REGIONAL_OFFICER)) {
                throw new UnauthorizedAccessException("Only Scrutiny Committee, Bureau, or Regional Officers can complete Scrutiny.");
            }
        } else if (target == ApplicationStage.EVC_SCHEDULED) {
            if (!SecurityUtils.hasRole(RoleConstants.ROLE_REGIONAL_OFFICER) &&
                !SecurityUtils.hasRole(RoleConstants.ROLE_EVC_COMMITTEE)) {
                throw new UnauthorizedAccessException("Only Regional Officers or EVC Committee can schedule inspection.");
            }
        }
    }

    private void updateStageSla(Application app, ApplicationStage stage, Instant now) {
        String approvalTypeCode = app.getApprovalType() != null ? app.getApprovalType().getCode() : null;
        SlaRule rule = slaRuleRepository.findByStageNameAndApprovalTypeCode(stage.name(), approvalTypeCode)
                .or(() -> slaRuleRepository.findFirstByStageNameAndActiveTrue(stage.name()))
                .orElse(null);

        if (rule != null) {
            app.setSlaDeadline(now.plus(rule.getMaxDurationDays(), ChronoUnit.DAYS));
            app.setSlaBreached(false);
        } else {
            // Default 14 days SLA if unconfigured
            app.setSlaDeadline(now.plus(14, ChronoUnit.DAYS));
            app.setSlaBreached(false);
        }
    }

    private void createTasksForNewStage(Application app, ApplicationStage stage) {
        String taskName;
        String assignedRole;

        switch (stage) {
            case SUBMITTED, SCRUTINY -> {
                taskName = "Conduct Document & Compliance Scrutiny";
                assignedRole = RoleConstants.ROLE_SCRUTINY_COMMITTEE;
            }
            case DEFICIENCY_COMMUNICATED -> {
                taskName = "Rectify Deficiencies & Submit Compliance";
                assignedRole = RoleConstants.ROLE_INSTITUTION;
            }
            case EVC_PENDING -> {
                taskName = "Constitute & Schedule Expert Visiting Committee";
                assignedRole = RoleConstants.ROLE_REGIONAL_OFFICER;
            }
            case EVC_SCHEDULED -> {
                taskName = "Conduct On-Site/Virtual Inspection & Submit Report";
                assignedRole = RoleConstants.ROLE_EVC_COMMITTEE;
            }
            case SAC_REVIEW -> {
                taskName = "Standing Appellate Committee Case Hearing & Assessment";
                assignedRole = RoleConstants.ROLE_SAC_COMMITTEE;
            }
            case APPROVAL_PENDING -> {
                taskName = "Statutory Approval Determination";
                assignedRole = RoleConstants.ROLE_COMPETENT_AUTHORITY;
            }
            default -> {
                return;
            }
        }

        WorkflowTask task = WorkflowTask.builder()
                .application(app)
                .taskName(taskName)
                .stageName(stage.name())
                .assignedRole(assignedRole)
                .status("PENDING")
                .createdAt(Instant.now())
                .build();
        workflowTaskRepository.save(task);

        // Notify officers of the assigned role
        notificationService.createNotification(
                null,
                assignedRole,
                "New Task Assigned: " + app.getApplicationNumber(),
                taskName + " for application " + app.getApplicationNumber(),
                "WORKFLOW_TASK",
                "HIGH",
                "APPLICATION",
                app.getId()
        );
    }
}
