package com.devops.backend.auth.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de recuperación/reinicio de contraseña")
public record PasswordResetResponse(
        @Schema(description = "Mensaje descriptivo de la operación", example = "Contraseña reiniciada exitosamente") String message,

        @Schema(description = "Indica si la operación fue exitosa", example = "true") Boolean success,

        @Schema(description = "Marca de tiempo de la respuesta (ISO-8601)", example = "2026-04-18T14:30:00") LocalDateTime timestamp) {
    /**
     * Constructor compacto que genera automáticamente el timestamp
     */
    public PasswordResetResponse(String message, Boolean success) {
        this(message, success, LocalDateTime.now());
    }
}
