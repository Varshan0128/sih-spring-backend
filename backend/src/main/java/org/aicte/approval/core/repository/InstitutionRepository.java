package org.aicte.approval.core.repository;

import org.aicte.approval.core.model.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByPermanentId(String permanentId);
    Optional<Institution> findByContactUserId(Long userId);
    List<Institution> findByRegion(String region);
}
