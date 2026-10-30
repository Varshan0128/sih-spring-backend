package org.aicte.approval;

import org.aicte.approval.common.exception.InvalidWorkflowActionException;
import org.aicte.approval.common.security.RoleConstants;
import org.aicte.approval.common.security.UserPrincipal;
import org.aicte.approval.core.model.Application;
import org.aicte.approval.core.model.ApplicationStage;
import org.aicte.approval.core.model.ApprovalType;
import org.aicte.approval.core.model.Institution;
import org.aicte.approval.core.repository.ApplicationRepository;
import org.aicte.approval.core.repository.ApplicationStageHistoryRepository;
import org.aicte.approval.core.repository.SlaRuleRepository;
import org.aicte.approval.core.repository.WorkflowTaskRepository;
import org.aicte.approval.core.service.WorkflowEngineService;
import org.aicte.approval.notification.service.AuditService;
import org.aicte.approval.notification.service.NotificationService;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowEngineTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationStageHistoryRepository stageHistoryRepository;

    @Mock
    private WorkflowTaskRepository workflowTaskRepository;

    @Mock
    private SlaRuleRepository slaRuleRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private WorkflowEngineService workflowEngineService;

    private Application testApp;

    @BeforeEach
    void setUp() {
        Institution institution = Institution.builder().id(1L).name("Test Institute").region("Northern").build();
        ApprovalType approvalType = ApprovalType.builder().code("NEW_INSTITUTE").name("New Institute").build();

        testApp = Application.builder()
                .id(100L)
                .applicationNumber("AICTE-2026-APP-100")
                .institution(institution)
                .approvalType(approvalType)
                .currentStage(ApplicationStage.SCRUTINY)
                .status("IN_PROGRESS")
                .build();
    }

    @Test
    @DisplayName("Should successfully transition from SCRUTINY to EVC_PENDING when authorized")
    void shouldTransitionFromScrutinyToEvcPending() {
        // Mock authenticated Scrutiny Committee member
        UserPrincipal principal = UserPrincipal.builder()
                .id(4L)
                .username("scrutiny_user")
                .authorities(List.of(new SimpleGrantedAuthority(RoleConstants.ROLE_SCRUTINY_COMMITTEE)))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        when(applicationRepository.findById(100L)).thenReturn(Optional.of(testApp));
        when(stageHistoryRepository.findFirstByApplicationIdAndStatusOrderByEnteredAtDesc(100L, "IN_PROGRESS"))
                .thenReturn(Optional.empty());
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Application result = workflowEngineService.transitionStage(
                100L, ApplicationStage.EVC_PENDING, "Recommended EVC inspection", "EVC_REQUIRED");

        assertNotNull(result);
        assertEquals(ApplicationStage.EVC_PENDING, result.getCurrentStage());
        verify(auditService, times(1)).recordEvent(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should reject illegal transition directly from DRAFT to APPROVED")
    void shouldRejectIllegalTransitionDirectlyFromDraftToApproved() {
        testApp.setCurrentStage(ApplicationStage.DRAFT);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(testApp));

        UserPrincipal principal = UserPrincipal.builder()
                .id(7L)
                .username("competent_auth")
                .authorities(List.of(new SimpleGrantedAuthority(RoleConstants.ROLE_COMPETENT_AUTHORITY)))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        assertThrows(InvalidWorkflowActionException.class, () ->
                workflowEngineService.transitionStage(100L, ApplicationStage.APPROVED, "Try bypass", "APPROVE"));
    }
}
