package org.aicte.approval.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class DocumentDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentResponseDto {
        private Long id;
        private Long applicationId;
        private Long institutionId;
        private String documentType;
        private String fileName;
        private Long fileSize;
        private String contentType;
        private Integer version;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private boolean isExpired;
        private boolean isVerified;
        private String status;
        private Instant uploadTimestamp;
        private ExtractionSummaryDto extractionResult;
        private String downloadUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractionSummaryDto {
        private String extractedInstitutionName;
        private String extractedDocType;
        private LocalDate extractedIssueDate;
        private LocalDate extractedExpiryDate;
        private BigDecimal confidenceScore;
        private String suspiciousFlags;
        private String validationStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyDocumentRequest {
        private boolean verified;
        private String remarks;
    }
}
