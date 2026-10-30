package org.aicte.approval.core.service;

import lombok.RequiredArgsConstructor;
import org.aicte.approval.common.exception.ResourceNotFoundException;
import org.aicte.approval.common.security.SecurityUtils;
import org.aicte.approval.core.dto.InstitutionDtos;
import org.aicte.approval.core.model.Institution;
import org.aicte.approval.core.repository.InstitutionRepository;
import org.aicte.approval.notification.service.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<InstitutionDtos.InstitutionResponseDto> getAllInstitutions() {
        return institutionRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InstitutionDtos.InstitutionResponseDto getInstitutionById(Long id) {
        SecurityUtils.validateInstitutionAccess(id);
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", id));
        return mapToDto(institution);
    }

    @Transactional
    public InstitutionDtos.InstitutionResponseDto createInstitution(InstitutionDtos.CreateInstitutionRequest request) {
        Institution institution = Institution.builder()
                .permanentId(request.getPermanentId())
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .region(request.getRegion())
                .pinCode(request.getPinCode())
                .institutionType(request.getInstitutionType())
                .status("ACTIVE")
                .build();

        Institution saved = institutionRepository.save(institution);
        auditService.recordEvent(
                "CREATE_INSTITUTION",
                "INSTITUTION",
                saved.getId().toString(),
                null,
                saved.getStatus(),
                null
        );

        return mapToDto(saved);
    }

    public InstitutionDtos.InstitutionResponseDto mapToDto(Institution inst) {
        return InstitutionDtos.InstitutionResponseDto.builder()
                .id(inst.getId())
                .permanentId(inst.getPermanentId())
                .name(inst.getName())
                .address(inst.getAddress())
                .city(inst.getCity())
                .state(inst.getState())
                .region(inst.getRegion())
                .pinCode(inst.getPinCode())
                .institutionType(inst.getInstitutionType())
                .status(inst.getStatus())
                .contactUserId(inst.getContactUser() != null ? inst.getContactUser().getId() : null)
                .contactUserName(inst.getContactUser() != null ? inst.getContactUser().getFullName() : null)
                .createdAt(inst.getCreatedAt())
                .build();
    }
}
