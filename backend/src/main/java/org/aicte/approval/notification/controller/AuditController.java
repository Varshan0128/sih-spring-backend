package org.aicte.approval.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.aicte.approval.common.dto.ApiResponse;
import org.aicte.approval.notification.model.AuditEvent;
import org.aicte.approval.notification.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit & Tamper-Evident History", description = "Immutable audit event logging and case timelines")
public class AuditController {

    private final AuditService auditService;

    @GetMapping({"", "/events"})
    @Operation(summary = "Get recent immutable audit events across the portal")
    public ResponseEntity<ApiResponse<List<AuditEvent>>> getRecentEvents(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getRecentEvents(limit)));
    }

    @GetMapping("/timeline/{entityName}/{entityId}")
    @Operation(summary = "Get tamper-evident audit timeline for an entity (e.g. APPLICATION 1024)")
    public ResponseEntity<ApiResponse<List<AuditEvent>>> getTimeline(
            @PathVariable String entityName,
            @PathVariable String entityId) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getEntityTimeline(entityName.toUpperCase(), entityId)));
    }

    @GetMapping({"/applications/{id}", "/application/{id}"})
    @Operation(summary = "Get audit timeline for an application")
    public ResponseEntity<ApiResponse<List<AuditEvent>>> getApplicationTimeline(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getEntityTimeline("APPLICATION", id.toString())));
    }

    @GetMapping("/verify-chain")
    @Operation(summary = "Verify cryptographic SHA-256 hash chaining of the audit ledger")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> verifyChain() {
        return ResponseEntity.ok(ApiResponse.success(auditService.verifyChainIntegrity()));
    }
}
