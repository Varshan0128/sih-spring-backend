package org.aicte.approval.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class ComplianceDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyComplianceRequest {
        private String status; // VERIFIED, REJECTED
        private String remarks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceResponseDto {
        private Long id;
        private Long applicationId;
        private String title;
        private String category;
        private String description;
        private Instant dueDate;
        private String status;
        private String verifiedByName;
        private Instant verifiedAt;
        private String verificationRemarks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UndertakingResponseDto {
        private Long id;
        private Long applicationId;
        private Long institutionId;
        private String title;
        private String content;
        private boolean isSigned;
        private Instant signedAt;
        private Instant deadline;
        private String status;
    }
}
