package org.aicte.approval.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "sla_breach_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaBreachRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "stage_name", nullable = false, length = 50)
    private String stageName;

    @Builder.Default
    @Column(name = "breach_timestamp", nullable = false)
    private Instant breachTimestamp = Instant.now();

    @Column(name = "sla_days", nullable = false)
    private Integer slaDays;

    @Column(name = "elapsed_days", nullable = false)
    private Integer elapsedDays;

    @Column(name = "escalated_to_role", nullable = false, length = 50)
    private String escalatedToRole;

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String status = "ESCALATED";

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
