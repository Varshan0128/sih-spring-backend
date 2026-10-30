package org.aicte.approval.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aicte.approval.common.dto.ApiResponse;
import org.aicte.approval.common.security.SecurityUtils;
import org.aicte.approval.common.security.UserPrincipal;
import org.aicte.approval.core.dto.AuthDtos;
import org.aicte.approval.core.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & Identity", description = "OAuth2/JWT token login, registration, and user identity")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with credentials to obtain JWT token")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        AuthDtos.AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/register")
    @Operation(summary = "Register new institution or officer account")
    public ResponseEntity<ApiResponse<AuthDtos.UserSummaryDto>> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        AuthDtos.UserSummaryDto response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "User registered successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<AuthDtos.UserSummaryDto>> getCurrentUser() {
        UserPrincipal user = SecurityUtils.getRequiredCurrentUser();
        List<String> roles = user.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());

        AuthDtos.UserSummaryDto dto = AuthDtos.UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .region(user.getRegion())
                .bureau(user.getBureau())
                .designation(user.getBureau())
                .institutionId(user.getInstitutionId())
                .build();

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
