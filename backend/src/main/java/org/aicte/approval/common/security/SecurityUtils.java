package org.aicte.approval.common.security;

import org.aicte.approval.common.exception.UnauthorizedAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return Optional.of((UserPrincipal) auth.getPrincipal());
        }
        return Optional.empty();
    }

    public static UserPrincipal getRequiredCurrentUser() {
        return getCurrentUser().orElseThrow(() ->
                new UnauthorizedAccessException("User is not authenticated"));
    }

    public static boolean hasRole(String role) {
        return getCurrentUser()
                .map(user -> user.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals(role)))
                .orElse(false);
    }

    public static boolean isSystemAdmin() {
        return hasRole(RoleConstants.ROLE_SYSTEM_ADMIN);
    }

    public static boolean isCompetentAuthority() {
        return hasRole(RoleConstants.ROLE_COMPETENT_AUTHORITY);
    }

    public static boolean isRegionalOfficer() {
        return hasRole(RoleConstants.ROLE_REGIONAL_OFFICER);
    }

    public static boolean isInstitutionUser() {
        return hasRole(RoleConstants.ROLE_INSTITUTION);
    }

    /**
     * Enforce institution-level authorization:
     * Institution users can ONLY access data belonging to their own institution.
     */
    public static void validateInstitutionAccess(Long targetInstitutionId) {
        if (isSystemAdmin() || isCompetentAuthority() || isRegionalOfficer()) {
            return; // Authorized AICTE officials can access permitted cases
        }
        UserPrincipal user = getRequiredCurrentUser();
        if (isInstitutionUser()) {
            if (user.getInstitutionId() == null || !user.getInstitutionId().equals(targetInstitutionId)) {
                throw new UnauthorizedAccessException("Access denied: You are not authorized to view or manage another institution's data.");
            }
        }
    }
}
