package org.aicte.approval.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aicte.approval.common.dto.ApiResponse;
import org.aicte.approval.core.dto.InstitutionDtos;
import org.aicte.approval.core.service.InstitutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/institutions")
@RequiredArgsConstructor
@Tag(name = "Institution Management", description = "Management of AICTE approved technical institutions")
public class InstitutionController {

    private final InstitutionService institutionService;

    @GetMapping
    @Operation(summary = "List all institutions")
    public ResponseEntity<ApiResponse<List<InstitutionDtos.InstitutionResponseDto>>> getAllInstitutions() {
        return ResponseEntity.ok(ApiResponse.success(institutionService.getAllInstitutions()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get institution details by ID")
    public ResponseEntity<ApiResponse<InstitutionDtos.InstitutionResponseDto>> getInstitutionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(institutionService.getInstitutionById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'REGIONAL_OFFICER')")
    @Operation(summary = "Create/Register a new technical institution")
    public ResponseEntity<ApiResponse<InstitutionDtos.InstitutionResponseDto>> createInstitution(
            @Valid @RequestBody InstitutionDtos.CreateInstitutionRequest request) {
        InstitutionDtos.InstitutionResponseDto response = institutionService.createInstitution(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Institution created successfully"));
    }
}
