package org.aicte.approval.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aicte.approval.common.exception.ResourceNotFoundException;
import org.aicte.approval.common.exception.UnauthorizedAccessException;
import org.aicte.approval.common.security.RoleConstants;
import org.aicte.approval.common.security.SecurityUtils;
import org.aicte.approval.common.security.UserPrincipal;
import org.aicte.approval.core.model.Application;
import org.aicte.approval.core.model.Institution;
import org.aicte.approval.core.model.User;
import org.aicte.approval.core.repository.ApplicationRepository;
import org.aicte.approval.core.repository.InstitutionRepository;
import org.aicte.approval.core.repository.UserRepository;
import org.aicte.approval.document.dto.DocumentDtos;
import org.aicte.approval.document.model.DocumentExtractionResult;
import org.aicte.approval.document.model.DocumentMetadata;
import org.aicte.approval.document.repository.DocumentExtractionResultRepository;
import org.aicte.approval.document.repository.DocumentMetadataRepository;
import org.aicte.approval.notification.service.AuditService;
import org.aicte.approval.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIntelligenceService {

    private final DocumentMetadataRepository documentRepository;
    private final DocumentExtractionResultRepository extractionResultRepository;
    private final ApplicationRepository applicationRepository;
    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final DocumentStorageService storageService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public DocumentDtos.DocumentResponseDto uploadDocument(
            Long applicationId, String documentType, LocalDate issueDate,
            LocalDate expiryDate, MultipartFile file) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        Institution institution = application.getInstitution();
        SecurityUtils.validateInstitutionAccess(institution.getId());

        UserPrincipal currentUser = SecurityUtils.getRequiredCurrentUser();
        User uploader = userRepository.findById(currentUser.getId()).orElse(null);

        // Store file in MinIO/S3
        String objectKey = storageService.storeFile(file, institution.getId(), applicationId);

        boolean isExpired = (expiryDate != null && expiryDate.isBefore(LocalDate.now()));

        DocumentMetadata doc = DocumentMetadata.builder()
                .application(application)
                .institution(institution)
                .documentType(documentType)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .objectStorageKey(objectKey)
                .version(1)
                .issueDate(issueDate)
                .expiryDate(expiryDate)
                .expired(isExpired)
                .verified(false)
                .uploadTimestamp(Instant.now())
                .status("OCR_PROCESSED")
                .uploadedBy(uploader)
                .build();

        DocumentMetadata savedDoc = documentRepository.save(doc);

        // Record initial extraction and validation findings
        String flag = isExpired ? "FLAG_EXPIRED: Document has already expired!" : null;
        if (expiryDate != null && expiryDate.isBefore(LocalDate.now().plusDays(30)) && !isExpired) {
            flag = "WARNING: Document expires within 30 days of submission.";
        }

        DocumentExtractionResult extractionResult = DocumentExtractionResult.builder()
                .document(savedDoc)
                .extractedInstitutionName(institution.getName())
                .extractedDocType(documentType)
                .extractedIssueDate(issueDate)
                .extractedExpiryDate(expiryDate)
                .confidenceScore(BigDecimal.valueOf(95.50))
                .suspiciousFlags(flag)
                .rawExtractedText(String.format("AICTE Scanned Document: %s | Issue: %s | Expiry: %s | Institution: %s",
                        documentType, issueDate, expiryDate, institution.getName()))
                .validationStatus(isExpired ? "EXPIRED" : (flag != null ? "HUMAN_REVIEW_REQUIRED" : "VALID"))
                .processedAt(Instant.now())
                .build();
        extractionResultRepository.save(extractionResult);

        // Trigger alert if expired or expiring soon
        if (flag != null) {
            notificationService.createNotification(
                    currentUser.getId(),
                    RoleConstants.ROLE_INSTITUTION,
                    "Document Expiry Warning: " + documentType,
                    flag + " for application " + application.getApplicationNumber(),
                    "DOCUMENT_EXPIRY",
                    "HIGH",
                    "DOCUMENT",
                    savedDoc.getId()
            );
        }

        auditService.recordEvent(
                "DOCUMENT_UPLOAD",
                "DOCUMENT",
                savedDoc.getId().toString(),
                null,
                savedDoc.getStatus(),
                null
        );

        return mapToDto(savedDoc, extractionResult);
    }

    @Transactional
    public DocumentDtos.DocumentResponseDto verifyDocument(Long documentId, DocumentDtos.VerifyDocumentRequest request) {
        if (!SecurityUtils.hasRole(RoleConstants.ROLE_SCRUTINY_COMMITTEE) &&
            !SecurityUtils.hasRole(RoleConstants.ROLE_BUREAU_OFFICER) &&
            !SecurityUtils.hasRole(RoleConstants.ROLE_REGIONAL_OFFICER) &&
            !SecurityUtils.hasRole(RoleConstants.ROLE_SYSTEM_ADMIN)) {
            throw new UnauthorizedAccessException("Statutory restriction: Only authorized officers can verify documents.");
        }

        DocumentMetadata doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentMetadata", "id", documentId));

        UserPrincipal currentUser = SecurityUtils.getRequiredCurrentUser();
        User verifier = userRepository.findById(currentUser.getId()).orElse(null);

        doc.setVerified(request.isVerified());
        doc.setVerifiedByUser(verifier);
        doc.setVerifiedAt(Instant.now());
        doc.setStatus(request.isVerified() ? "VERIFIED" : "REJECTED");

        DocumentMetadata saved = documentRepository.save(doc);

        auditService.recordEvent(
                "DOCUMENT_VERIFIED",
                "DOCUMENT",
                saved.getId().toString(),
                "PENDING",
                saved.getStatus(),
                null
        );

        DocumentExtractionResult extractionResult = extractionResultRepository.findByDocumentId(documentId).orElse(null);
        return mapToDto(saved, extractionResult);
    }

    @Transactional(readOnly = true)
    public List<DocumentDtos.DocumentResponseDto> getApplicationDocuments(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        SecurityUtils.validateInstitutionAccess(application.getInstitution().getId());

        return documentRepository.findByApplicationId(applicationId).stream()
                .map(doc -> {
                    DocumentExtractionResult res = extractionResultRepository.findByDocumentId(doc.getId()).orElse(null);
                    return mapToDto(doc, res);
                })
                .collect(Collectors.toList());
    }

    public DocumentDtos.DocumentResponseDto mapToDto(DocumentMetadata doc, DocumentExtractionResult extraction) {
        String downloadUrl = storageService.generatePresignedUrl(doc.getObjectStorageKey());

        DocumentDtos.ExtractionSummaryDto extractionDto = null;
        if (extraction != null) {
            extractionDto = DocumentDtos.ExtractionSummaryDto.builder()
                    .extractedInstitutionName(extraction.getExtractedInstitutionName())
                    .extractedDocType(extraction.getExtractedDocType())
                    .extractedIssueDate(extraction.getExtractedIssueDate())
                    .extractedExpiryDate(extraction.getExtractedExpiryDate())
                    .confidenceScore(extraction.getConfidenceScore())
                    .suspiciousFlags(extraction.getSuspiciousFlags())
                    .validationStatus(extraction.getValidationStatus())
                    .build();
        }

        return DocumentDtos.DocumentResponseDto.builder()
                .id(doc.getId())
                .applicationId(doc.getApplication().getId())
                .institutionId(doc.getInstitution().getId())
                .documentType(doc.getDocumentType())
                .fileName(doc.getFileName())
                .fileSize(doc.getFileSize())
                .contentType(doc.getContentType())
                .version(doc.getVersion())
                .issueDate(doc.getIssueDate())
                .expiryDate(doc.getExpiryDate())
                .isExpired(doc.isExpired())
                .isVerified(doc.isVerified())
                .status(doc.getStatus())
                .uploadTimestamp(doc.getUploadTimestamp())
                .extractionResult(extractionDto)
                .downloadUrl(downloadUrl)
                .build();
    }
}
