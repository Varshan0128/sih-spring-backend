package org.aicte.approval.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aicte.approval.common.exception.ResourceNotFoundException;
import org.aicte.approval.common.exception.UnauthorizedAccessException;
import org.aicte.approval.common.security.RoleConstants;
import org.aicte.approval.common.security.SecurityUtils;
import org.aicte.approval.common.security.UserPrincipal;
import org.aicte.approval.core.dto.ApplicationDtos;
import org.aicte.approval.core.model.*;
import org.aicte.approval.core.repository.*;
import org.aicte.approval.notification.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final InstitutionRepository institutionRepository;
    private final ApprovalTypeRepository approvalTypeRepository;
    private final ApplicationStageHistoryRepository stageHistoryRepository;
    private final UserRepository userRepository;
    private final WorkflowEngineService workflowEngineService;
    private final AuditService auditService;

    @Transactional
    public ApplicationDtos.ApplicationResponseDto createApplication(ApplicationDtos.CreateApplicationRequest request) {
        UserPrincipal currentUser = SecurityUtils.getRequiredCurrentUser();

        final Long targetInstId = (request.getInstitutionId() != null) ?
                request.getInstitutionId() : currentUser.getInstitutionId();

        if (targetInstId == null) {
            throw new IllegalArgumentException("Institution ID must be specified");
        }

        SecurityUtils.validateInstitutionAccess(targetInstId);

        Institution institution = institutionRepository.findById(targetInstId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", targetInstId));

        ApprovalType approvalType = approvalTypeRepository.findByCode(request.getApprovalTypeCode())
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalType", "code", request.getApprovalTypeCode()));

        // Generate unique application number e.g. AICTE-2026-APP-7492
        int randomSuffix = 1000 + new Random().nextInt(9000);
        String appNumber = String.format("AICTE-%d-APP-%d", Year.now().getValue(), randomSuffix);

        User submittingUser = userRepository.findById(currentUser.getId()).orElse(null);

        Application application = Application.builder()
                .applicationNumber(appNumber)
                .institution(institution)
                .approvalType(approvalType)
                .academicYear(request.getAcademicYear() != null ? request.getAcademicYear() : "2026-2027")
                .currentStage(ApplicationStage.DRAFT)
                .status("IN_PROGRESS")
                .submittedBy(submittingUser)
                .riskScore(10)
                .riskLevel("LOW")
                .delayProbability(BigDecimal.valueOf(0.10))
                .primaryBottleneck("Draft - Pending Submission")
                .createdAt(Instant.now())
                .build();

        Application saved = applicationRepository.save(application);

        // Record initial history
        ApplicationStageHistory initialStage = ApplicationStageHistory.builder()
                .application(saved)
                .stageName(ApplicationStage.DRAFT.name())
                .enteredAt(Instant.now())
                .status("IN_PROGRESS")
                .completedByUser(submittingUser)
                .remarks("Draft created by institution user")
                .build();
        stageHistoryRepository.save(initialStage);

        auditService.recordEvent(
                "CREATE_APPLICATION_DRAFT",
                "APPLICATION",
                saved.getId().toString(),
                null,
                "DRAFT",
                null
        );

        return mapToDto(saved);
    }

    @Transactional
    public ApplicationDtos.ApplicationResponseDto submitApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        SecurityUtils.validateInstitutionAccess(application.getInstitution().getId());

        if (application.getCurrentStage() != ApplicationStage.DRAFT) {
            throw new IllegalArgumentException("Application is already submitted (current stage: " + application.getCurrentStage() + ")");
        }

        application.setSubmissionDate(Instant.now());
        applicationRepository.save(application);

        // Route to SUBMITTED and then SCRUTINY
        Application updated = workflowEngineService.transitionStage(
                applicationId,
                ApplicationStage.SUBMITTED,
                "Submitted by institution with official undertaking",
                "SUBMIT"
        );

        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public ApplicationDtos.ApplicationResponseDto getApplicationById(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        SecurityUtils.validateInstitutionAccess(application.getInstitution().getId());
        return mapToDto(application);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationDtos.ApplicationResponseDto> getApplications(
            String stage, String riskLevel, String region, Pageable pageable) {
        UserPrincipal currentUser = SecurityUtils.getRequiredCurrentUser();

        Long filterInstitutionId = null;
        String filterRegion = region;

        if (SecurityUtils.isInstitutionUser()) {
            filterInstitutionId = currentUser.getInstitutionId();
        } else if (SecurityUtils.isRegionalOfficer()) {
            // Regional officers can access cases within their regional jurisdiction
            if (currentUser.getRegion() != null && !currentUser.getRegion().isBlank()) {
                filterRegion = currentUser.getRegion();
            }
        }

        ApplicationStage stageEnum = null;
        if (stage != null && !stage.isBlank()) {
            try {
                stageEnum = ApplicationStage.valueOf(stage.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        return applicationRepository.filterApplications(
                filterInstitutionId, filterRegion, stageEnum, riskLevel, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDtos.StageHistoryDto> getApplicationStageHistory(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        SecurityUtils.validateInstitutionAccess(application.getInstitution().getId());

        return stageHistoryRepository.findByApplicationIdOrderByEnteredAtAsc(applicationId).stream()
                .map(sh -> ApplicationDtos.StageHistoryDto.builder()
                        .id(sh.getId())
                        .stageName(sh.getStageName())
                        .enteredAt(sh.getEnteredAt())
                        .exitedAt(sh.getExitedAt())
                        .durationHours(sh.getDurationHours())
                        .status(sh.getStatus())
                        .completedByName(sh.getCompletedByUser() != null ? sh.getCompletedByUser().getFullName() : null)
                        .remarks(sh.getRemarks())
                        .build())
                .collect(Collectors.toList());
    }

    public ApplicationDtos.ApplicationResponseDto mapToDto(Application app) {
        return ApplicationDtos.ApplicationResponseDto.builder()
                .id(app.getId())
                .applicationNumber(app.getApplicationNumber())
                .institutionId(app.getInstitution() != null ? app.getInstitution().getId() : null)
                .institutionName(app.getInstitution() != null ? app.getInstitution().getName() : null)
                .institutionPermanentId(app.getInstitution() != null ? app.getInstitution().getPermanentId() : null)
                .region(app.getInstitution() != null ? app.getInstitution().getRegion() : null)
                .approvalTypeCode(app.getApprovalType() != null ? app.getApprovalType().getCode() : null)
                .approvalTypeName(app.getApprovalType() != null ? app.getApprovalType().getName() : null)
                .academicYear(app.getAcademicYear())
                .currentStage(app.getCurrentStage())
                .status(app.getStatus())
                .submissionDate(app.getSubmissionDate())
                .slaDeadline(app.getSlaDeadline())
                .slaBreached(app.isSlaBreached())
                .riskScore(app.getRiskScore())
                .riskLevel(app.getRiskLevel())
                .delayProbability(app.getDelayProbability())
                .primaryBottleneck(app.getPrimaryBottleneck())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
