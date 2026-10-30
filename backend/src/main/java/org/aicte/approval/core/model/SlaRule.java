package org.aicte.approval.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "sla_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stage_name", nullable = false, length = 50)
    private String stageName;

    @Column(name = "approval_type_code", length = 50)
    private String approvalTypeCode;

    @Column(name = "max_duration_days", nullable = false)
    private Integer maxDurationDays;

    @Column(name = "warning_threshold_days", nullable = false)
    private Integer warningThresholdDays;

    @Column(name = "escalation_role", nullable = false, length = 50)
    private String escalationRole;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
