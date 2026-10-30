package org.aicte.approval.document.model;

import jakarta.persistence.*;
import lombok.*;
import org.aicte.approval.core.model.Application;
import org.aicte.approval.core.model.Institution;
import org.aicte.approval.core.model.User;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "document_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "document_type", nullable = false, length = 100)
    private String documentType;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "object_storage_key", nullable = false)
    private String objectStorageKey;

    @Builder.Default
    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Builder.Default
    @Column(name = "is_expired", nullable = false)
    private boolean expired = false;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_user_id")
    private User verifiedByUser;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Builder.Default
    @Column(name = "upload_timestamp", updatable = false)
    private Instant uploadTimestamp = Instant.now();

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String status = "PENDING_OCR";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;
}
