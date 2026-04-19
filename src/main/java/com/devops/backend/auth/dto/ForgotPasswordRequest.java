package com.devops.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record ForgotPasswordRequest(
        @NotBlank(message = "El correo electrónico no puede estar vacío") @Email(message = "El correo electrónico debe ser válido") @Schema(description = "Correo electrónico de la cuenta para iniciar recuperación de contraseña", example = "jperez@example.com") String email) {
}
