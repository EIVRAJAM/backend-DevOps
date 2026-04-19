package com.devops.backend.acceso.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de acceso del usuario (vista limitada sin campos sensibles o de auditoría)")
public record AccesoUserDTO(
                @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres") @Schema(description = "Nombre de usuario (editable)", example = "jperez") String username,

                @Email(message = "El correo debe ser válido") @Schema(description = "Correo electrónico (editable)", example = "jperez@example.com") String correoAcceso) {
}