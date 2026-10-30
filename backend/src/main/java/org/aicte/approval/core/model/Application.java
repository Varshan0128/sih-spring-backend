package org.aicte.approval.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_number", nullable = false, unique = true, length = 50)
    private String applicationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_type_id", nullable = false)
    private ApprovalType approvalType;

    @Builder.Default
    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear = "2026-2027";

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false, length = 50)
    private ApplicationStage currentStage;

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String status = "IN_PROGRESS";

    @Column(name = "submission_date")
    private Instant submissionDate;

    @Column(name = "sla_deadline")
    private Instant slaDeadline;

    @Builder.Default
    @Column(name = "is_sla_breached", nullable = false)
    private boolean slaBreached = false;

    @Builder.Default
    @Column(name = "risk_score")
    private Integer riskScore = 0;

    @Builder.Default
    @Column(name = "risk_level", length = 20)
    private String riskLevel = "LOW";

    @Builder.Default
    @Column(name = "delay_probability", precision = 5, scale = 2)
    private BigDecimal delayProbability = BigDecimal.ZERO;

    @Column(name = "primary_bottleneck", columnDefinition = "TEXT")
    private String primaryBottleneck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private User submittedBy;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
