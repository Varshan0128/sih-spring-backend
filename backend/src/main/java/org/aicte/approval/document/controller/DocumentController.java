package org.aicte.approval.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aicte.approval.common.dto.ApiResponse;
import org.aicte.approval.document.dto.DocumentDtos;
import org.aicte.approval.document.service.DocumentIntelligenceService;
import org.aicte.approval.document.service.DocumentStorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document Intelligence & Storage", description = "Secure document uploads, MinIO object storage, OCR processing, and verification")
public class DocumentController {

    private final DocumentIntelligenceService documentService;
    private final DocumentStorageService storageService;

    @PostMapping(value = "/applications/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload application document with OCR and expiry extraction")
    public ResponseEntity<ApiResponse<DocumentDtos.DocumentResponseDto>> uploadDocument(
            @PathVariable Long id,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "issueDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDate,
            @RequestParam(value = "expiryDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @RequestParam("file") MultipartFile file) {

        DocumentDtos.DocumentResponseDto response = documentService.uploadDocument(
                id, documentType, issueDate, expiryDate, file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Document uploaded and OCR processed successfully"));
    }

    @GetMapping({"/applications/{id}", "/application/{id}"})
    @Operation(summary = "List all documents and OCR validation results for an application")
    public ResponseEntity<ApiResponse<List<DocumentDtos.DocumentResponseDto>>> getApplicationDocuments(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getApplicationDocuments(id)));
    }

    @PostMapping("/{id}/verify")
    @Operation(summary = "Statutory officer verification of uploaded document")
    public ResponseEntity<ApiResponse<DocumentDtos.DocumentResponseDto>> verifyDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentDtos.VerifyDocumentRequest request) {
        DocumentDtos.DocumentResponseDto response = documentService.verifyDocument(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Document verification updated"));
    }

    @GetMapping("/download-stream")
    @Operation(summary = "Stream document file securely")
    public ResponseEntity<InputStreamResource> downloadStream(@RequestParam("key") String objectKey) {
        InputStream stream = storageService.getFileStream(objectKey);
        if (stream == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document.pdf\"")
                .body(new InputStreamResource(stream));
    }
}
