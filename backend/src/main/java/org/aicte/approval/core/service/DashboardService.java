package org.aicte.approval.core.service;

import lombok.RequiredArgsConstructor;
import org.aicte.approval.common.security.RoleConstants;
import org.aicte.approval.common.security.SecurityUtils;
import org.aicte.approval.common.security.UserPrincipal;
import org.aicte.approval.core.dto.ApplicationDtos;
import org.aicte.approval.core.dto.DashboardDtos;
import org.aicte.approval.core.model.Application;
import org.aicte.approval.core.model.ApplicationStage;
import org.aicte.approval.core.repository.ApplicationRepository;
import org.aicte.approval.core.repository.EvcRecordRepository;
import org.aicte.approval.core.repository.WorkflowTaskRepository;
import org.aicte.approval.notification.repository.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ApplicationRepository applicationRepository;
    private final EvcRecordRepository evcRecordRepository;
    private final NotificationRepository notificationRepository;
    private final ApplicationService applicationService;

    @Transactional(readOnly = true)
    public DashboardDtos.DashboardSummaryDto getDashboardSummary() {
        UserPrincipal currentUser = SecurityUtils.getRequiredCurrentUser();
        String primaryRole = currentUser.getAuthorities().isEmpty() ? "ROLE_INSTITUTION" :
                currentUser.getAuthorities().iterator().next().getAuthority();

        Long totalApps = applicationRepository.count();
        Long inProgress = applicationRepository.countByStatus("IN_PROGRESS");
        Long approved = applicationRepository.countByStatus("APPROVED");
        Long rejected = applicationRepository.countByStatus("REJECTED");
        Long slaBreached = applicationRepository.countBySlaBreachedTrue();
        Long highRisk = applicationRepository.countByRiskLevel("HIGH");
        Long pendingEvc = evcRecordRepository.countByStatus("OVERDUE") + evcRecordRepository.countByStatus("NOT_SCHEDULED");
        Long unreadNotifs = notificationRepository.countByRecipientUserIdAndReadFalse(currentUser.getId());

        // Stage distribution
        Map<String, Long> stageDist = new HashMap<>();
        for (ApplicationStage stage : ApplicationStage.values()) {
            long count = applicationRepository.countByCurrentStage(stage);
            if (count > 0) {
                stageDist.put(stage.name(), count);
            }
        }

        // Risk distribution
        Map<String, Long> riskDist = new HashMap<>();
        riskDist.put("LOW", applicationRepository.countByRiskLevel("LOW"));
        riskDist.put("MEDIUM", applicationRepository.countByRiskLevel("MEDIUM"));
        riskDist.put("HIGH", applicationRepository.countByRiskLevel("HIGH"));

        // Top urgent cases (AI prioritized queue)
        List<ApplicationDtos.ApplicationResponseDto> urgentCases = applicationRepository
                .findTopPriorityApplications(PageRequest.of(0, 5))
                .stream()
                .map(applicationService::mapToDto)
                .collect(Collectors.toList());

        // Recent applications
        List<ApplicationDtos.ApplicationResponseDto> recent = applicationRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(applicationService::mapToDto)
                .collect(Collectors.toList());

        String scopeDescription = "National Jurisdiction";
        if (SecurityUtils.isInstitutionUser()) {
            scopeDescription = "Institution Portal";
        } else if (SecurityUtils.isRegionalOfficer()) {
            scopeDescription = currentUser.getRegion() != null ?
                    currentUser.getRegion() + " Regional Office" : "Regional Office";
        }

        return DashboardDtos.DashboardSummaryDto.builder()
                .userRole(primaryRole)
                .userName(currentUser.getFullName())
                .scope(scopeDescription)
                .totalApplications(totalApps)
                .inProgressCount(inProgress)
                .approvedCount(approved)
                .rejectedCount(rejected)
                .slaBreachedCount(slaBreached)
                .highRiskCount(highRisk)
                .pendingEvcCount(pendingEvc)
                .unreadNotificationsCount(unreadNotifs)
                .stageDistribution(stageDist)
                .riskDistribution(riskDist)
                .urgentCases(urgentCases)
                .recentApplications(recent)
                .build();
    }
}
