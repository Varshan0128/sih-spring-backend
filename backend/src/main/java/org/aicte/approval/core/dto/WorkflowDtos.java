package org.aicte.approval.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class WorkflowDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowTaskDto {
        private Long id;
        private Long applicationId;
        private String applicationNumber;
        private String institutionName;
        private String taskName;
        private String stageName;
        private Long assignedToUserId;
        private String assignedToName;
        private String assignedRole;
        private String status;
        private String comments;
        private String confidentialNotes;
        private Instant createdAt;
        private Instant completedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageDefinitionDto {
        private String code;
        private String name;
        private String description;
        private int defaultDurationDays;
        private String responsibleRole;
    }
}
