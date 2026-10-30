package org.aicte.approval.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    @Builder.Default
    private boolean success = false;
    private int status;
    private String error;
    private String message;
    private String path;
    @Builder.Default
    private Instant timestamp = Instant.now();
    private List<String> validationErrors;
}
