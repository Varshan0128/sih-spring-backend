package org.aicte.approval.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.aicte.approval.common.dto.ApiResponse;
import org.aicte.approval.notification.dto.ReportDtos;
import org.aicte.approval.notification.service.ReportingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports & Analytics", description = "Workload, ageing, overdue, and institutional compliance reports")
public class ReportController {

    private final ReportingService reportingService;

    @GetMapping("/compliance")
    @Operation(summary = "Generate institutional compliance report")
    public ResponseEntity<ApiResponse<ReportDtos.ComplianceReportDto>> getComplianceReport(
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(ApiResponse.success(reportingService.generateComplianceReport(academicYear)));
    }

    @GetMapping("/sla-ageing")
    @Operation(summary = "Generate stage ageing and SLA adherence report")
    public ResponseEntity<ApiResponse<ReportDtos.SlaAgeingReportDto>> getSlaAgeingReport() {
        return ResponseEntity.ok(ApiResponse.success(reportingService.generateSlaAgeingReport()));
    }

    @GetMapping({"/regional-summary", "/regional"})
    @Operation(summary = "Generate consolidated regional performance and SLA summary")
    public ResponseEntity<ApiResponse<java.util.List<java.util.Map<String, Object>>>> getRegionalSummary() {
        return ResponseEntity.ok(ApiResponse.success(reportingService.generateRegionalSummary()));
    }
}
