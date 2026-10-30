package org.aicte.approval.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.aicte.approval.common.dto.ApiResponse;
import org.aicte.approval.core.dto.DashboardDtos;
import org.aicte.approval.core.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard & Analytics", description = "Role-scoped enterprise dashboard summaries and action metrics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping({"/summary", "/metrics"})
    @Operation(summary = "Get personalized role-scoped dashboard metrics, bottlenecks, and urgent queues")
    public ResponseEntity<ApiResponse<DashboardDtos.DashboardSummaryDto>> getDashboardSummary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDashboardSummary()));
    }
}
