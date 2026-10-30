package org.aicte.approval.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

public class ReportDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceReportDto {
        private String academicYear;
        private Instant generatedAt;
        private long totalCases;
        private long compliantCount;
        private long deficientCount;
        private long pendingVerificationCount;
        private Map<String, Long> deficiencyCategoryBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlaAgeingReportDto {
        private Instant generatedAt;
        private long totalActiveCases;
        private long withinSlaCount;
        private long approachingSlaCount;
        private long breachedSlaCount;
        private double averageStageAgeDays;
        private Map<String, Long> stageWiseAgeingDays;
        private Map<String, Long> regionalWorkload;
    }
}
