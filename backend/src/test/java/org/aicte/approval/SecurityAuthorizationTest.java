package org.aicte.approval;

import org.aicte.approval.common.exception.UnauthorizedAccessException;
import org.aicte.approval.common.security.RoleConstants;
import org.aicte.approval.common.security.SecurityUtils;
import org.aicte.approval.common.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityAuthorizationTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Institution user should be rejected when accessing another institution's data")
    void shouldDenyCrossInstitutionAccess() {
        UserPrincipal institutionUser = UserPrincipal.builder()
                .id(1L)
                .username("inst_user_1")
                .institutionId(1L) // Belongs to Institution 1
                .authorities(List.of(new SimpleGrantedAuthority(RoleConstants.ROLE_INSTITUTION)))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(institutionUser, null, institutionUser.getAuthorities()));

        // Attempting to access Institution 2
        assertThrows(UnauthorizedAccessException.class, () ->
                SecurityUtils.validateInstitutionAccess(2L));
    }

    @Test
    @DisplayName("Institution user should be allowed to access their own institution data")
    void shouldAllowSelfInstitutionAccess() {
        UserPrincipal institutionUser = UserPrincipal.builder()
                .id(1L)
                .username("inst_user_1")
                .institutionId(1L)
                .authorities(List.of(new SimpleGrantedAuthority(RoleConstants.ROLE_INSTITUTION)))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(institutionUser, null, institutionUser.getAuthorities()));

        assertDoesNotThrow(() -> SecurityUtils.validateInstitutionAccess(1L));
    }

    @Test
    @DisplayName("Regional Officer or System Administrator can access permitted institutions")
    void shouldAllowOfficersInstitutionAccess() {
        UserPrincipal regionalOfficer = UserPrincipal.builder()
                .id(2L)
                .username("reg_officer")
                .region("Northern")
                .authorities(List.of(new SimpleGrantedAuthority(RoleConstants.ROLE_REGIONAL_OFFICER)))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(regionalOfficer, null, regionalOfficer.getAuthorities()));

        assertDoesNotThrow(() -> SecurityUtils.validateInstitutionAccess(2L));
    }
}
