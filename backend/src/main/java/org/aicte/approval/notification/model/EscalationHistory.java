package org.aicte.approval.notification.model;

import jakarta.persistence.*;
import lombok.*;
import org.aicte.approval.core.model.Application;

import java.time.Instant;

@Entity
@Table(name = "escalation_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscalationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "from_role", nullable = false, length = 50)
    private String fromRole;

    @Column(name = "to_role", nullable = false, length = 50)
    private String toRole;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Builder.Default
    @Column(name = "triggered_by", nullable = false, length = 50)
    private String triggeredBy = "AUTO_SLA";

    @Builder.Default
    @Column(name = "escalated_at", updatable = false)
    private Instant escalatedAt = Instant.now();

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    @Column(name = "resolution_summary", columnDefinition = "TEXT")
    private String resolutionSummary;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
