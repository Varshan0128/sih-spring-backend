package org.aicte.approval.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aicte.approval.common.dto.ApiResponse;
import org.aicte.approval.common.dto.PagedResponse;
import org.aicte.approval.core.dto.ApplicationDtos;
import org.aicte.approval.core.service.ApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications & Lifecycle", description = "Application creation, tracking, submission, and status monitoring")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @Operation(summary = "Create a new application draft")
    public ResponseEntity<ApiResponse<ApplicationDtos.ApplicationResponseDto>> createApplication(
            @Valid @RequestBody ApplicationDtos.CreateApplicationRequest request) {
        ApplicationDtos.ApplicationResponseDto response = applicationService.createApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Application draft created successfully"));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit application for AICTE scrutiny")
    public ResponseEntity<ApiResponse<ApplicationDtos.ApplicationResponseDto>> submitApplication(
            @PathVariable Long id) {
        ApplicationDtos.ApplicationResponseDto response = applicationService.submitApplication(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Application submitted successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application details by ID")
    public ResponseEntity<ApiResponse<ApplicationDtos.ApplicationResponseDto>> getApplicationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getApplicationById(id)));
    }

    @GetMapping
    @Operation(summary = "List and filter applications with role-scoped access")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationDtos.ApplicationResponseDto>>> getApplications(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ApplicationDtos.ApplicationResponseDto> pagedData =
                applicationService.getApplications(stage, riskLevel, region, pageable);

        PagedResponse<ApplicationDtos.ApplicationResponseDto> response = PagedResponse.<ApplicationDtos.ApplicationResponseDto>builder()
                .content(pagedData.getContent())
                .page(pagedData.getNumber())
                .size(pagedData.getSize())
                .totalElements(pagedData.getTotalElements())
                .totalPages(pagedData.getTotalPages())
                .last(pagedData.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get application stage history timeline")
    public ResponseEntity<ApiResponse<List<ApplicationDtos.StageHistoryDto>>> getStageHistory(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getApplicationStageHistory(id)));
    }
}
