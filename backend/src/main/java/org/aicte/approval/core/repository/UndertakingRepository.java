package org.aicte.approval.core.repository;

import org.aicte.approval.core.model.Undertaking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UndertakingRepository extends JpaRepository<Undertaking, Long> {
    List<Undertaking> findByApplicationId(Long applicationId);
    List<Undertaking> findByInstitutionId(Long institutionId);
}
