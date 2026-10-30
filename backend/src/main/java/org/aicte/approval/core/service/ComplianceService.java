package org.aicte.approval.core.service;

import lombok.RequiredArgsConstructor;
import org.aicte.approval.common.exception.ResourceNotFoundException;
import org.aicte.approval.common.exception.UnauthorizedAccessException;
import org.aicte.approval.common.security.RoleConstants;
import org.aicte.approval.common.security.SecurityUtils;
import org.aicte.approval.common.security.UserPrincipal;
import org.aicte.approval.core.dto.ComplianceDtos;
import org.aicte.approval.core.model.Application;
import org.aicte.approval.core.model.ComplianceRequirement;
import org.aicte.approval.core.model.Undertaking;
import org.aicte.approval.core.model.User;
import org.aicte.approval.core.repository.ApplicationRepository;
import org.aicte.approval.core.repository.ComplianceRequirementRepository;
import org.aicte.approval.core.repository.UndertakingRepository;
import org.aicte.approval.core.repository.UserRepository;
import org.aicte.approval.notification.service.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplianceService {

    private final ComplianceRequirementRepository complianceRepository;
    private final UndertakingRepository undertakingRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<ComplianceDtos.ComplianceResponseDto> getApplicationCompliance(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        SecurityUtils.validateInstitutionAccess(application.getInstitution().getId());

        return complianceRepository.findByApplicationId(applicationId).stream()
                .map(this::mapToComplianceDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComplianceDtos.ComplianceResponseDto verifyCompliance(Long requirementId, ComplianceDtos.VerifyComplianceRequest request) {
        if (!SecurityUtils.hasRole(RoleConstants.ROLE_SCRUTINY_COMMITTEE) &&
            !SecurityUtils.hasRole(RoleConstants.ROLE_BUREAU_OFFICER) &&
            !SecurityUtils.hasRole(RoleConstants.ROLE_REGIONAL_OFFICER) &&
            !SecurityUtils.hasRole(RoleConstants.ROLE_SYSTEM_ADMIN)) {
            throw new UnauthorizedAccessException("Only authorized scrutiny/processing officers can verify compliance.");
        }

        ComplianceRequirement req = complianceRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceRequirement", "id", requirementId));

        UserPrincipal currentUser = SecurityUtils.getRequiredCurrentUser();
        User verifier = userRepository.findById(currentUser.getId()).orElse(null);

        req.setStatus(request.getStatus());
        req.setVerifiedByUser(verifier);
        req.setVerifiedAt(Instant.now());
        req.setVerificationRemarks(request.getRemarks());

        ComplianceRequirement saved = complianceRepository.save(req);

        auditService.recordEvent(
                "COMPLIANCE_VERIFIED",
                "COMPLIANCE_REQUIREMENT",
                saved.getId().toString(),
                "PENDING",
                saved.getStatus(),
                null
        );

        return mapToComplianceDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ComplianceDtos.UndertakingResponseDto> getApplicationUndertakings(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        SecurityUtils.validateInstitutionAccess(application.getInstitution().getId());

        return undertakingRepository.findByApplicationId(applicationId).stream()
                .map(this::mapToUndertakingDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComplianceDtos.UndertakingResponseDto signUndertaking(Long undertakingId) {
        Undertaking undertaking = undertakingRepository.findById(undertakingId)
                .orElseThrow(() -> new ResourceNotFoundException("Undertaking", "id", undertakingId));

        SecurityUtils.validateInstitutionAccess(undertaking.getInstitution().getId());

        UserPrincipal currentUser = SecurityUtils.getRequiredCurrentUser();
        User signer = userRepository.findById(currentUser.getId()).orElse(null);

        undertaking.setSigned(true);
        undertaking.setSignedAt(Instant.now());
        undertaking.setSignedByUser(signer);
        undertaking.setStatus("ACCEPTED");

        Undertaking saved = undertakingRepository.save(undertaking);

        auditService.recordEvent(
                "UNDERTAKING_SIGNED",
                "UNDERTAKING",
                saved.getId().toString(),
                "PENDING",
                "ACCEPTED",
                null
        );

        return mapToUndertakingDto(saved);
    }

    public ComplianceDtos.ComplianceResponseDto mapToComplianceDto(ComplianceRequirement req) {
        return ComplianceDtos.ComplianceResponseDto.builder()
                .id(req.getId())
                .applicationId(req.getApplication().getId())
                .title(req.getTitle())
                .category(req.getCategory())
                .description(req.getDescription())
                .dueDate(req.getDueDate())
                .status(req.getStatus())
                .verifiedByName(req.getVerifiedByUser() != null ? req.getVerifiedByUser().getFullName() : null)
                .verifiedAt(req.getVerifiedAt())
                .verificationRemarks(req.getVerificationRemarks())
                .build();
    }

    public ComplianceDtos.UndertakingResponseDto mapToUndertakingDto(Undertaking u) {
        return ComplianceDtos.UndertakingResponseDto.builder()
                .id(u.getId())
                .applicationId(u.getApplication().getId())
                .institutionId(u.getInstitution().getId())
                .title(u.getTitle())
                .content(u.getContent())
                .isSigned(u.isSigned())
                .signedAt(u.getSignedAt())
                .deadline(u.getDeadline())
                .status(u.getStatus())
                .build();
    }
}
