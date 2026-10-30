package org.aicte.approval.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "evc_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvcRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    @Builder.Default
    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String status = "NOT_SCHEDULED";

    @Column(name = "scheduled_date")
    private Instant scheduledDate;

    @Builder.Default
    @Column(name = "inspection_mode", length = 30)
    private String inspectionMode = "PHYSICAL";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committee_chairman_id")
    private User committeeChairman;

    @Column(name = "committee_members", columnDefinition = "TEXT")
    private String committeeMembers;

    @Column(name = "report_url")
    private String reportUrl;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Builder.Default
    @Column(name = "compliance_status", length = 50)
    private String complianceStatus = "PENDING";

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
