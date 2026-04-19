package com.devops.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(
                @NotBlank(message = "El correo o username es obligatorio") @Schema(description = "Nombre de usuario o correo electrónico registrado en el sistema", example = "jperez") String usernameOrEmail,

                @NotBlank(message = "La contraseña es obligatoria") @Schema(description = "Contraseña asociada a la cuenta", example = "SecurePass123") String password) {
}
