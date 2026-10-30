package org.aicte.approval.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aicte.approval.common.exception.ResourceNotFoundException;
import org.aicte.approval.common.security.JwtTokenProvider;
import org.aicte.approval.common.security.RoleConstants;
import org.aicte.approval.common.security.UserPrincipal;
import org.aicte.approval.core.dto.AuthDtos;
import org.aicte.approval.core.model.Institution;
import org.aicte.approval.core.model.Role;
import org.aicte.approval.core.model.User;
import org.aicte.approval.core.repository.InstitutionRepository;
import org.aicte.approval.core.repository.RoleRepository;
import org.aicte.approval.core.repository.UserRepository;
import org.aicte.approval.notification.service.AuditService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(userPrincipal);

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        auditService.recordEvent(
                "USER_LOGIN",
                "USER",
                userPrincipal.getId().toString(),
                null,
                "AUTHENTICATED",
                null
        );

        return AuthDtos.AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .expiresInMs(86400000L) // 24 hours
                .user(AuthDtos.UserSummaryDto.builder()
                        .id(userPrincipal.getId())
                        .username(userPrincipal.getUsername())
                        .email(userPrincipal.getEmail())
                        .fullName(userPrincipal.getFullName())
                        .roles(roles)
                        .region(userPrincipal.getRegion())
                        .bureau(userPrincipal.getBureau())
                        .designation(userPrincipal.getBureau())
                        .institutionId(userPrincipal.getInstitutionId())
                        .build())
                .build();
    }

    @Transactional
    public AuthDtos.UserSummaryDto register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String roleName = request.getRoleName() != null ? request.getRoleName() : RoleConstants.ROLE_INSTITUTION;
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .region(request.getRegion())
                .bureau(request.getBureau())
                .designation(request.getDesignation())
                .active(true)
                .roles(Set.of(role))
                .build();

        User savedUser = userRepository.save(user);

        Long instId = request.getInstitutionId();
        if (instId != null) {
            Institution institution = institutionRepository.findById(instId)
                    .orElseThrow(() -> new ResourceNotFoundException("Institution not found with id: " + instId));
            institution.setContactUser(savedUser);
            institutionRepository.save(institution);
        }

        auditService.recordEvent(
                "USER_REGISTERED",
                "USER",
                savedUser.getId().toString(),
                null,
                "CREATED",
                null
        );

        return AuthDtos.UserSummaryDto.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .roles(Collections.singletonList(role.getName()))
                .region(savedUser.getRegion())
                .bureau(savedUser.getBureau())
                .designation(savedUser.getDesignation())
                .institutionId(instId)
                .build();
    }
}
