package org.aicte.approval.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "application_stage_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "stage_name", nullable = false, length = 50)
    private String stageName;

    @Builder.Default
    @Column(name = "entered_at", nullable = false)
    private Instant enteredAt = Instant.now();

    @Column(name = "exited_at")
    private Instant exitedAt;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by_user_id")
    private User completedByUser;

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String status = "IN_PROGRESS";

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
