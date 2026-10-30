package org.aicte.approval.core.repository;

import org.aicte.approval.core.model.Application;
import org.aicte.approval.core.model.ApplicationStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByApplicationNumber(String applicationNumber);

    Page<Application> findByInstitutionId(Long institutionId, Pageable pageable);

    Page<Application> findByCurrentStage(ApplicationStage currentStage, Pageable pageable);

    @Query("SELECT a FROM Application a WHERE a.institution.region = :region")
    Page<Application> findByRegion(@Param("region") String region, Pageable pageable);

    @Query("SELECT a FROM Application a WHERE (:institutionId IS NULL OR a.institution.id = :institutionId) " +
           "AND (:region IS NULL OR a.institution.region = :region) " +
           "AND (:stage IS NULL OR a.currentStage = :stage) " +
           "AND (:riskLevel IS NULL OR a.riskLevel = :riskLevel)")
    Page<Application> filterApplications(
            @Param("institutionId") Long institutionId,
            @Param("region") String region,
            @Param("stage") ApplicationStage stage,
            @Param("riskLevel") String riskLevel,
            Pageable pageable);

    long countByCurrentStage(ApplicationStage currentStage);

    long countByStatus(String status);

    long countBySlaBreachedTrue();

    long countByRiskLevel(String riskLevel);

    @Query("SELECT a FROM Application a WHERE a.slaDeadline <= :now AND a.status = 'IN_PROGRESS' AND a.slaBreached = false")
    List<Application> findApproachingOrBreachedApplications(@Param("now") Instant now);

    @Query("SELECT a FROM Application a WHERE a.status = 'IN_PROGRESS' ORDER BY a.riskScore DESC, a.slaDeadline ASC")
    List<Application> findTopPriorityApplications(Pageable pageable);
}
