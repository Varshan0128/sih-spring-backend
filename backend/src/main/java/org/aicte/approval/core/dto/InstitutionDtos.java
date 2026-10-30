package org.aicte.approval.core.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class InstitutionDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateInstitutionRequest {
        @NotBlank(message = "Permanent ID is required")
        private String permanentId;

        @NotBlank(message = "Institution name is required")
        private String name;

        @NotBlank(message = "Address is required")
        private String address;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "State is required")
        private String state;

        @NotBlank(message = "Region is required")
        private String region;

        @NotBlank(message = "PIN code is required")
        private String pinCode;

        @NotBlank(message = "Institution type is required")
        private String institutionType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstitutionResponseDto {
        private Long id;
        private String permanentId;
        private String name;
        private String address;
        private String city;
        private String state;
        private String region;
        private String pinCode;
        private String institutionType;
        private String status;
        private Long contactUserId;
        private String contactUserName;
        private Instant createdAt;
    }
}
