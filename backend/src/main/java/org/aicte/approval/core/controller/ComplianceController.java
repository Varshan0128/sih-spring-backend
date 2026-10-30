package org.aicte.approval.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aicte.approval.common.dto.ApiResponse;
import org.aicte.approval.core.dto.ComplianceDtos;
import org.aicte.approval.core.service.ComplianceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/compliance")
@RequiredArgsConstructor
@Tag(name = "Compliance & Undertakings", description = "Statutory compliance verification and legal undertakings")
public class ComplianceController {

    private final ComplianceService complianceService;

    @GetMapping({"/applications/{id}/requirements", "/application/{id}", "/applications/{id}"})
    @Operation(summary = "List compliance requirements for an application")
    public ResponseEntity<ApiResponse<List<ComplianceDtos.ComplianceResponseDto>>> getRequirements(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(complianceService.getApplicationCompliance(id)));
    }

    @PostMapping("/requirements/{id}/verify")
    @Operation(summary = "Verify or reject a compliance requirement")
    public ResponseEntity<ApiResponse<ComplianceDtos.ComplianceResponseDto>> verifyRequirement(
            @PathVariable Long id,
            @Valid @RequestBody ComplianceDtos.VerifyComplianceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(complianceService.verifyCompliance(id, request), "Compliance verified"));
    }

    @GetMapping({"/applications/{id}/undertakings", "/undertakings/application/{id}", "/undertakings/{id}"})
    @Operation(summary = "List legal undertakings required for an application")
    public ResponseEntity<ApiResponse<List<ComplianceDtos.UndertakingResponseDto>>> getUndertakings(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(complianceService.getApplicationUndertakings(id)));
    }

    @PostMapping("/undertakings/{id}/sign")
    @Operation(summary = "Digitally sign a mandatory statutory undertaking")
    public ResponseEntity<ApiResponse<ComplianceDtos.UndertakingResponseDto>> signUndertaking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(complianceService.signUndertaking(id), "Undertaking signed successfully"));
    }
}
