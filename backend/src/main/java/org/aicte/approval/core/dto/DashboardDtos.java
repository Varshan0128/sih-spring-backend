package org.aicte.approval.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class DashboardDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardSummaryDto {
        private String userRole;
        private String userName;
        private String scope;
        private long totalApplications;
        private long inProgressCount;
        private long approvedCount;
        private long rejectedCount;
        private long slaBreachedCount;
        private long highRiskCount;
        private long pendingEvcCount;
        private long unreadNotificationsCount;
        private Map<String, Long> stageDistribution;
        private Map<String, Long> riskDistribution;
        private List<ApplicationDtos.ApplicationResponseDto> urgentCases;
        private List<ApplicationDtos.ApplicationResponseDto> recentApplications;
    }
}
