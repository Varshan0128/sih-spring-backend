package org.aicte.approval.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aicte.approval.ai.dto.AiDtos;
import org.aicte.approval.ai.service.AiIntegrationService;
import org.aicte.approval.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Intelligence & Early Warning", description = "Risk scoring (0-100), delay prediction, bottleneck root-cause analysis, smart queues, and RAG assistant")
public class AiController {

    private final AiIntegrationService aiService;

    @RequestMapping(value = {"/applications/{id}/risk-score", "/risk-score/{id}"}, method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Calculate 0-100 case health/risk score with contributing signals")
    public ResponseEntity<ApiResponse<AiDtos.RiskScoreResponse>> getRiskScore(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(aiService.calculateRiskScore(id)));
    }

    @RequestMapping(value = {"/applications/{id}/delay-prediction", "/delay-prediction/{id}"}, method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Predict approval delay probability and estimated overdue days")
    public ResponseEntity<ApiResponse<AiDtos.DelayPredictionResponse>> predictDelay(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(aiService.predictDelay(id)));
    }

    @RequestMapping(value = {"/applications/{id}/bottleneck", "/bottleneck/{id}"}, method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Why is this case stuck? Evidence-based primary bottleneck explanation")
    public ResponseEntity<ApiResponse<AiDtos.BottleneckResponse>> explainBottleneck(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(aiService.explainBottleneck(id)));
    }

    @RequestMapping(value = {"/applications/{id}/summary", "/case-summary/{id}"}, method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Generate automatic executive case summary for authorized officers")
    public ResponseEntity<ApiResponse<AiDtos.CaseSummaryResponse>> getCaseSummary(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(aiService.generateCaseSummary(id)));
    }

    @RequestMapping(value = {"/applications/{id}/anomaly-detection", "/anomaly-detection/{id}"}, method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Detect abnormal processing durations or document patterns")
    public ResponseEntity<ApiResponse<AiDtos.AnomalyResponse>> detectAnomaly(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(aiService.detectAnomalies(id)));
    }

    @GetMapping("/priority-queue")
    @Operation(summary = "Get AI-prioritized action queue ranked by urgency, SLA risk, and dependencies")
    public ResponseEntity<ApiResponse<List<AiDtos.PriorityQueueItemDto>>> getPriorityQueue() {
        return ResponseEntity.ok(ApiResponse.success(aiService.getSmartPriorityQueue()));
    }

    @PostMapping("/assistant")
    @Operation(summary = "Ask AI Assistant with RAG retrieval from APH rules and authorized application facts")
    public ResponseEntity<ApiResponse<AiDtos.AssistantQueryResponse>> askAssistant(
            @Valid @RequestBody AiDtos.AssistantQueryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(aiService.answerAssistantQuery(request)));
    }
}
