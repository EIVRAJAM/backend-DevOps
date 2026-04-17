package com.devops.backend.auth.dto;

import java.time.LocalDateTime;

public record PasswordResetResponse(
        String message,
        Boolean success,
        LocalDateTime timestamp) {
    /**
     * Constructor compacto que genera automáticamente el timestamp
     */
    public PasswordResetResponse(String message, Boolean success) {
        this(message, success, LocalDateTime.now());
    }
}
