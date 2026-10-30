package org.aicte.approval.document.repository;

import org.aicte.approval.document.model.DocumentExtractionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentExtractionResultRepository extends JpaRepository<DocumentExtractionResult, Long> {
    Optional<DocumentExtractionResult> findByDocumentId(Long documentId);
}
