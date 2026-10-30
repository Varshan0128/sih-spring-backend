package org.aicte.approval.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class AiDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskScoreResponse {
        private Long applicationId;
        private String applicationNumber;
        private Integer riskScore; // 0 - 100
        private String riskLevel; // LOW, MEDIUM, HIGH
        private BigDecimal delayProbability;
        private List<String> contributingFactors;
        private String modelVersion;
        private boolean isBaseline;
        private String baselineExplanation;
        private Instant timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BottleneckResponse {
        private Long applicationId;
        private String applicationNumber;
        private String currentStage;
        private String bottleneckStage;
        private String primaryBottleneck;
        private Integer daysInCurrentStage;
        private Integer slaLimitDays;
        private List<String> evidenceFactors;
        private String recommendedOfficialAction;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaseSummaryResponse {
        private Long applicationId;
        private String applicationNumber;
        private String institutionName;
        private String currentStage;
        private String summaryText;
        private String pendingAction;
        private String primaryRisk;
        private List<String> keyMilestones;
        private Instant slaDeadline;
        private Instant generatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriorityQueueItemDto {
        private Long applicationId;
        private String applicationNumber;
        private String institutionName;
        private String region;
        private String stage;
        private Integer riskScore;
        private String riskLevel;
        private Integer priorityRank;
        private String urgencyReason;
        private Instant slaDeadline;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssistantQueryRequest {
        private String query;
        private Long contextApplicationId; // Optional specific application context
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssistantQueryResponse {
        private String query;
        private String answer;
        private List<String> citations;
        private List<String> authorizedSources;
        private boolean accessPermitted;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnomalyResponse {
        private Long applicationId;
        private String applicationNumber;
        private String anomalyType; // UNUSUAL_STAGE_DURATION, REPEATED_RETURNS, DOCUMENT_IRREGULARITY
        private String description;
        private String severity;
        private String evidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DelayPredictionResponse {
        private Long applicationId;
        private String applicationNumber;
        private String currentStage;
        private String riskLevel;
        private BigDecimal delayProbability;
        private Integer estimatedDelayDays;
        private String modelVersion;
        private List<String> contributingFactors;
        private Instant predictionTimestamp;
    }
}
