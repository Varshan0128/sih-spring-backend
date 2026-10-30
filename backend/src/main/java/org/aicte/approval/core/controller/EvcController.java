package org.aicte.approval.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aicte.approval.common.dto.ApiResponse;
import org.aicte.approval.core.dto.EvcDtos;
import org.aicte.approval.core.service.EvcService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/evc")
@RequiredArgsConstructor
@Tag(name = "EVC & Expert Committee", description = "Expert Visiting Committee inspection scheduling and report submission")
public class EvcController {

    private final EvcService evcService;

    @GetMapping({"", "/all"})
    @Operation(summary = "List all EVC inspection records")
    public ResponseEntity<ApiResponse<java.util.List<EvcDtos.EvcResponseDto>>> getAllEvcRecords() {
        return ResponseEntity.ok(ApiResponse.success(evcService.getAllEvcRecords()));
    }

    @GetMapping({"/applications/{id}", "/{id}"})
    @Operation(summary = "Get EVC record and inspection status for application")
    public ResponseEntity<ApiResponse<EvcDtos.EvcResponseDto>> getEvcDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(evcService.getEvcByApplicationId(id)));
    }

    @PostMapping("/applications/{id}/schedule")
    @Operation(summary = "Schedule EVC inspection date and assign chairman/members")
    public ResponseEntity<ApiResponse<EvcDtos.EvcResponseDto>> scheduleEvc(
            @PathVariable Long id,
            @Valid @RequestBody EvcDtos.ScheduleEvcRequest request) {
        return ResponseEntity.ok(ApiResponse.success(evcService.scheduleEvc(id, request), "EVC scheduled successfully"));
    }

    @PostMapping("/applications/{id}/report")
    @Operation(summary = "Submit EVC inspection report and compliance findings")
    public ResponseEntity<ApiResponse<EvcDtos.EvcResponseDto>> submitReport(
            @PathVariable Long id,
            @Valid @RequestBody EvcDtos.EvcReportRequest request) {
        return ResponseEntity.ok(ApiResponse.success(evcService.submitReport(id, request), "EVC report submitted successfully"));
    }
}
