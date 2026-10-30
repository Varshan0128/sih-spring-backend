package org.aicte.approval.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "workflow_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;

    @Column(name = "stage_name", nullable = false, length = 50)
    private String stageName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedToUser;

    @Column(name = "assigned_role", nullable = false, length = 50)
    private String assignedRole;

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String comments;

    /**
     * Confidential committee notes - strictly restricted from institution view
     */
    @Column(name = "confidential_notes", columnDefinition = "TEXT")
    private String confidentialNotes;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;
}
