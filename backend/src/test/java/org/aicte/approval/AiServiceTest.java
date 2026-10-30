package org.aicte.approval;

import org.aicte.approval.ai.dto.AiDtos;
import org.aicte.approval.ai.service.AiIntegrationService;
import org.aicte.approval.common.security.RoleConstants;
import org.aicte.approval.common.security.UserPrincipal;
import org.aicte.approval.core.model.Application;
import org.aicte.approval.core.model.ApplicationStage;
import org.aicte.approval.core.model.ApprovalType;
import org.aicte.approval.core.model.EvcRecord;
import org.aicte.approval.core.model.Institution;
import org.aicte.approval.core.repository.ApplicationRepository;
import org.aicte.approval.core.repository.ComplianceRequirementRepository;
import org.aicte.approval.core.repository.EvcRecordRepository;
import org.aicte.approval.document.repository.DocumentMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private EvcRecordRepository evcRecordRepository;

    @Mock
    private ComplianceRequirementRepository complianceRepository;

    @Mock
    private DocumentMetadataRepository documentMetadataRepository;

    @InjectMocks
    private AiIntegrationService aiIntegrationService;

    private Application testApp;

    @BeforeEach
    void setUp() {
        Institution inst = Institution.builder().id(1L).name("DIT Noida").region("Northern").build();
        ApprovalType at = ApprovalType.builder().code("NEW_INSTITUTE").name("New").build();

        testApp = Application.builder()
                .id(1024L)
                .applicationNumber("AICTE-2026-APP-1024")
                .institution(inst)
                .approvalType(at)
                .currentStage(ApplicationStage.EVC_PENDING)
                .status("IN_PROGRESS")
                .slaDeadline(Instant.now().minusSeconds(86400 * 2)) // Breached by 2 days
                .slaBreached(true)
                .build();

        UserPrincipal principal = UserPrincipal.builder()
                .id(2L)
                .username("officer")
                .authorities(List.of(new SimpleGrantedAuthority(RoleConstants.ROLE_REGIONAL_OFFICER)))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @Test
    @DisplayName("Should generate High Risk score and contributing factors for overdue EVC case")
    void shouldGenerateHighRiskScoreForOverdueCase() {
        when(applicationRepository.findById(1024L)).thenReturn(Optional.of(testApp));
        when(evcRecordRepository.findByApplicationId(1024L)).thenReturn(Optional.of(
                EvcRecord.builder().status("OVERDUE").build()
        ));
        when(documentMetadataRepository.findDocumentsExpiringBefore(any())).thenReturn(Collections.emptyList());
        when(complianceRepository.countByApplicationIdAndStatus(1024L, "PENDING")).thenReturn(1L);

        AiDtos.RiskScoreResponse response = aiIntegrationService.calculateRiskScore(1024L);

        assertNotNull(response);
        assertTrue(response.getRiskScore() >= 70, "Score should reflect high risk (>= 70)");
        assertEquals("HIGH", response.getRiskLevel());
        assertTrue(response.isBaseline());
        assertEquals("v1.0.0-rules-baseline", response.getModelVersion());
        assertFalse(response.getContributingFactors().isEmpty());
    }

    @Test
    @DisplayName("Should explain EVC bottleneck when application is stuck in EVC_PENDING")
    void shouldExplainEvcBottleneck() {
        when(applicationRepository.findById(1024L)).thenReturn(Optional.of(testApp));

        AiDtos.BottleneckResponse response = aiIntegrationService.explainBottleneck(1024L);

        assertNotNull(response);
        assertEquals("EVC_PENDING", response.getBottleneckStage());
        assertTrue(response.getPrimaryBottleneck().contains("EVC scheduling pending"));
        assertNotNull(response.getRecommendedOfficialAction());
    }
}
