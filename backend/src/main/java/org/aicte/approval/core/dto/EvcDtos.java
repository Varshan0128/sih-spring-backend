package org.aicte.approval.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class EvcDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleEvcRequest {
        private Instant scheduledDate;
        private String inspectionMode; // PHYSICAL, VIRTUAL, HYBRID
        private Long chairmanUserId;
        private String committeeMembers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvcReportRequest {
        private String observations;
        private String complianceStatus; // COMPLIANT, DEFICIENT
        private String reportUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvcResponseDto {
        private Long id;
        private Long applicationId;
        private String applicationNumber;
        private String institutionName;
        private boolean isRequired;
        private String status;
        private Instant scheduledDate;
        private String inspectionMode;
        private Long chairmanUserId;
        private String chairmanName;
        private String committeeMembers;
        private String reportUrl;
        private String observations;
        private String complianceStatus;
    }
}
