package org.aicte.approval.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aicte.approval.common.dto.ApiResponse;
import org.aicte.approval.common.security.RoleConstants;
import org.aicte.approval.common.security.SecurityUtils;
import org.aicte.approval.common.security.UserPrincipal;
import org.aicte.approval.core.dto.ApplicationDtos;
import org.aicte.approval.core.dto.WorkflowDtos;
import org.aicte.approval.core.model.Application;
import org.aicte.approval.core.model.ApplicationStage;
import org.aicte.approval.core.model.WorkflowTask;
import org.aicte.approval.core.repository.WorkflowTaskRepository;
import org.aicte.approval.core.service.ApplicationService;
import org.aicte.approval.core.service.WorkflowEngineService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
@Tag(name = "Workflow & Approvals", description = "BPMN state engine, stage transitions, and committee task routing")
public class WorkflowController {

    private final WorkflowEngineService workflowEngineService;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final ApplicationService applicationService;

    @GetMapping("/stages")
    @Operation(summary = "List all statutory workflow stages and processing rules")
    public ResponseEntity<ApiResponse<List<WorkflowDtos.StageDefinitionDto>>> getWorkflowStages() {
        List<WorkflowDtos.StageDefinitionDto> stages = Arrays.stream(ApplicationStage.values())
                .map(stage -> WorkflowDtos.StageDefinitionDto.builder()
                        .code(stage.name())
                        .name(stage.name().replace('_', ' '))
                        .description("Statutory approval stage: " + stage.name())
                        .defaultDurationDays(14)
                        .responsibleRole(stage == ApplicationStage.SCRUTINY ? "ROLE_SCRUTINY_COMMITTEE" :
                                stage == ApplicationStage.EVC_PENDING || stage == ApplicationStage.EVC_SCHEDULED ? "ROLE_EVC_COMMITTEE" :
                                stage == ApplicationStage.SAC_REVIEW ? "ROLE_SAC_COMMITTEE" : "ROLE_REGIONAL_OFFICER")
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(stages));
    }

    @PostMapping("/applications/{id}/transition")
    @Operation(summary = "Perform authorized statutory stage transition")
    public ResponseEntity<ApiResponse<ApplicationDtos.ApplicationResponseDto>> transitionStage(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationDtos.StageTransitionRequest request) {
        Application updated = workflowEngineService.transitionStage(
                id, request.getTargetStage(), request.getRemarks(), request.getAction());
        return ResponseEntity.ok(ApiResponse.success(applicationService.mapToDto(updated), "Stage transitioned successfully"));
    }

    @GetMapping({"/tasks", "/tasks/my-tasks"})
    @Operation(summary = "Get active tasks assigned to the current officer or committee")
    public ResponseEntity<ApiResponse<Page<WorkflowDtos.WorkflowTaskDto>>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserPrincipal user = SecurityUtils.getRequiredCurrentUser();
        Page<WorkflowTask> tasks = workflowTaskRepository.findByAssignedToUserIdAndStatus(
                user.getId(), "PENDING", PageRequest.of(page, size));
        
        boolean isInst = SecurityUtils.isInstitutionUser();
        Page<WorkflowDtos.WorkflowTaskDto> dtoPage = tasks.map(t -> WorkflowDtos.WorkflowTaskDto.builder()
                .id(t.getId())
                .applicationId(t.getApplication() != null ? t.getApplication().getId() : null)
                .applicationNumber(t.getApplication() != null ? t.getApplication().getApplicationNumber() : null)
                .institutionName(t.getApplication() != null && t.getApplication().getInstitution() != null ?
                        t.getApplication().getInstitution().getName() : null)
                .taskName(t.getTaskName())
                .stageName(t.getStageName())
                .assignedToUserId(t.getAssignedToUser() != null ? t.getAssignedToUser().getId() : null)
                .assignedToName(t.getAssignedToUser() != null ? t.getAssignedToUser().getFullName() : null)
                .assignedRole(t.getAssignedRole())
                .status(t.getStatus())
                .comments(t.getComments())
                .confidentialNotes(isInst ? null : t.getConfidentialNotes())
                .createdAt(t.getCreatedAt())
                .completedAt(t.getCompletedAt())
                .build());

        return ResponseEntity.ok(ApiResponse.success(dtoPage));
    }
}
