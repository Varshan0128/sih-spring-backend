package org.aicte.approval.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "undertakings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Undertaking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(name = "is_signed", nullable = false)
    private boolean signed = false;

    @Column(name = "signed_at")
    private Instant signedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signed_by_user_id")
    private User signedByUser;

    @Column(nullable = false)
    private Instant deadline;

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
