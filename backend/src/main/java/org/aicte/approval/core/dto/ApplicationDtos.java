package org.aicte.approval.core.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aicte.approval.core.model.ApplicationStage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class ApplicationDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateApplicationRequest {
        @NotBlank(message = "Approval type code is required (e.g. NEW_INSTITUTE, EXTENSION_OF_APPROVAL)")
        private String approvalTypeCode;

        private String academicYear = "2026-2027";
        private Long institutionId; // Optional if logged-in user is an institution
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationResponseDto {
        private Long id;
        private String applicationNumber;
        private Long institutionId;
        private String institutionName;
        private String institutionPermanentId;
        private String region;
        private String approvalTypeCode;
        private String approvalTypeName;
        private String academicYear;
        private ApplicationStage currentStage;
        private String status;
        private Instant submissionDate;
        private Instant slaDeadline;
        private boolean slaBreached;
        private Integer riskScore;
        private String riskLevel;
        private BigDecimal delayProbability;
        private String primaryBottleneck;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageTransitionRequest {
        private ApplicationStage targetStage;
        private String remarks;
        private String action; // APPROVE, REJECT, REQUEST_DEFICIENCY, RETURN_CORRECTION
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskActionRequest {
        @NotBlank(message = "Action is required")
        private String action; // e.g. COMPLETED, DEFICIENCY_FOUND, RETURNED, ESCALATED
        private String comments;
        private String confidentialNotes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageHistoryDto {
        private Long id;
        private String stageName;
        private Instant enteredAt;
        private Instant exitedAt;
        private Integer durationHours;
        private String status;
        private String completedByName;
        private String remarks;
    }
}
