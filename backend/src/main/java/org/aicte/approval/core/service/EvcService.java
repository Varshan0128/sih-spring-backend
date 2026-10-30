package org.aicte.approval.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aicte.approval.common.exception.ResourceNotFoundException;
import org.aicte.approval.common.exception.UnauthorizedAccessException;
import org.aicte.approval.common.security.RoleConstants;
import org.aicte.approval.common.security.SecurityUtils;
import org.aicte.approval.core.dto.EvcDtos;
import org.aicte.approval.core.model.Application;
import org.aicte.approval.core.model.ApplicationStage;
import org.aicte.approval.core.model.EvcRecord;
import org.aicte.approval.core.model.User;
import org.aicte.approval.core.repository.ApplicationRepository;
import org.aicte.approval.core.repository.EvcRecordRepository;
import org.aicte.approval.core.repository.UserRepository;
import org.aicte.approval.notification.service.AuditService;
import org.aicte.approval.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvcService {

    private final EvcRecordRepository evcRecordRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final WorkflowEngineService workflowEngineService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public EvcDtos.EvcResponseDto scheduleEvc(Long applicationId, EvcDtos.ScheduleEvcRequest request) {
        if (!SecurityUtils.hasRole(RoleConstants.ROLE_REGIONAL_OFFICER) &&
            !SecurityUtils.hasRole(RoleConstants.ROLE_SYSTEM_ADMIN) &&
            !SecurityUtils.hasRole(RoleConstants.ROLE_EVC_COMMITTEE)) {
            throw new UnauthorizedAccessException("Only Regional Officers or authorized EVC Leads can schedule inspections.");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        User chairman = null;
        if (request.getChairmanUserId() != null) {
            chairman = userRepository.findById(request.getChairmanUserId()).orElse(null);
        }

        EvcRecord evc = evcRecordRepository.findByApplicationId(applicationId)
                .orElseGet(() -> EvcRecord.builder().application(application).build());

        evc.setStatus("SCHEDULED");
        evc.setScheduledDate(request.getScheduledDate());
        evc.setInspectionMode(request.getInspectionMode() != null ? request.getInspectionMode() : "PHYSICAL");
        evc.setCommitteeChairman(chairman);
        evc.setCommitteeMembers(request.getCommitteeMembers());

        EvcRecord saved = evcRecordRepository.save(evc);

        // Advance stage to EVC_SCHEDULED
        if (application.getCurrentStage() == ApplicationStage.EVC_PENDING) {
            workflowEngineService.transitionStage(
                    applicationId,
                    ApplicationStage.EVC_SCHEDULED,
                    "EVC Inspection scheduled for " + request.getScheduledDate(),
                    "SCHEDULE_EVC"
            );
        }

        // Notify Institution and Committee
        notificationService.createNotification(
                null,
                RoleConstants.ROLE_INSTITUTION,
                "EVC Inspection Scheduled: " + application.getApplicationNumber(),
                "Your institution inspection has been scheduled for " + request.getScheduledDate() +
                        " in " + evc.getInspectionMode() + " mode.",
                "EVC_SCHEDULE",
                "HIGH",
                "APPLICATION",
                applicationId
        );

        auditService.recordEvent(
                "EVC_SCHEDULED",
                "EVC_RECORD",
                saved.getId().toString(),
                "EVC_PENDING",
                "EVC_SCHEDULED",
                null
        );

        return mapToDto(saved);
    }

    @Transactional
    public EvcDtos.EvcResponseDto submitReport(Long applicationId, EvcDtos.EvcReportRequest request) {
        if (!SecurityUtils.hasRole(RoleConstants.ROLE_EVC_COMMITTEE) &&
            !SecurityUtils.hasRole(RoleConstants.ROLE_SYSTEM_ADMIN)) {
            throw new UnauthorizedAccessException("Only EVC committee members can submit inspection findings.");
        }

        EvcRecord evc = evcRecordRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("EvcRecord", "applicationId", applicationId));

        evc.setStatus("COMPLETED");
        evc.setObservations(request.getObservations());
        evc.setComplianceStatus(request.getComplianceStatus());
        evc.setReportUrl(request.getReportUrl());

        EvcRecord saved = evcRecordRepository.save(evc);

        // Advance stage to EVC_COMPLETED
        workflowEngineService.transitionStage(
                applicationId,
                ApplicationStage.EVC_COMPLETED,
                "EVC Report submitted with compliance status: " + request.getComplianceStatus(),
                "SUBMIT_EVC_REPORT"
        );

        auditService.recordEvent(
                "EVC_REPORT_SUBMITTED",
                "EVC_RECORD",
                saved.getId().toString(),
                "EVC_SCHEDULED",
                "EVC_COMPLETED",
                null
        );

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<EvcDtos.EvcResponseDto> getAllEvcRecords() {
        return evcRecordRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EvcDtos.EvcResponseDto getEvcByApplicationId(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        SecurityUtils.validateInstitutionAccess(application.getInstitution().getId());

        EvcRecord evc = evcRecordRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("EvcRecord", "applicationId", applicationId));

        return mapToDto(evc);
    }

    public EvcDtos.EvcResponseDto mapToDto(EvcRecord evc) {
        return EvcDtos.EvcResponseDto.builder()
                .id(evc.getId())
                .applicationId(evc.getApplication().getId())
                .applicationNumber(evc.getApplication().getApplicationNumber())
                .institutionName(evc.getApplication().getInstitution().getName())
                .isRequired(evc.isRequired())
                .status(evc.getStatus())
                .scheduledDate(evc.getScheduledDate())
                .inspectionMode(evc.getInspectionMode())
                .chairmanUserId(evc.getCommitteeChairman() != null ? evc.getCommitteeChairman().getId() : null)
                .chairmanName(evc.getCommitteeChairman() != null ? evc.getCommitteeChairman().getFullName() : null)
                .committeeMembers(evc.getCommitteeMembers())
                .reportUrl(evc.getReportUrl())
                .observations(evc.getObservations())
                .complianceStatus(evc.getComplianceStatus())
                .build();
    }
}
