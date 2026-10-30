package org.aicte.approval.document.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "document_extraction_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentExtractionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private DocumentMetadata document;

    @Column(name = "extracted_institution_name")
    private String extractedInstitutionName;

    @Column(name = "extracted_doc_type", length = 100)
    private String extractedDocType;

    @Column(name = "extracted_issue_date")
    private LocalDate extractedIssueDate;

    @Column(name = "extracted_expiry_date")
    private LocalDate extractedExpiryDate;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "suspicious_flags", columnDefinition = "TEXT")
    private String suspiciousFlags;

    @Column(name = "raw_extracted_text", columnDefinition = "TEXT")
    private String rawExtractedText;

    @Builder.Default
    @Column(name = "validation_status", nullable = false, length = 50)
    private String validationStatus = "HUMAN_REVIEW_REQUIRED";

    @Builder.Default
    @Column(name = "processed_at", updatable = false)
    private Instant processedAt = Instant.now();
}
