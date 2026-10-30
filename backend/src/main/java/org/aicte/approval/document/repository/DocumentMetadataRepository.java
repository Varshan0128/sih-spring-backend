package org.aicte.approval.document.repository;

import org.aicte.approval.document.model.DocumentMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, Long> {
    List<DocumentMetadata> findByApplicationId(Long applicationId);
    List<DocumentMetadata> findByInstitutionId(Long institutionId);

    @Query("SELECT d FROM DocumentMetadata d WHERE d.expiryDate <= :expiryThreshold AND d.expired = false")
    List<DocumentMetadata> findDocumentsExpiringBefore(@Param("expiryThreshold") LocalDate expiryThreshold);

    long countByApplicationIdAndVerifiedFalse(Long applicationId);
}
