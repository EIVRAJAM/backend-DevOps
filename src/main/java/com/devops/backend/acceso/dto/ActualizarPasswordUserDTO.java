package com.devops.backend.acceso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para cambio de contraseña por el usuario (requiere validar contraseña actual)")
public record ActualizarPasswordUserDTO(
        @NotBlank(message = "La contraseña actual no puede estar vacía") @Schema(description = "Contraseña actual del usuario (se valida para confirmar identidad)", example = "OldSecurePass123") String passwordActual,

        @NotBlank(message = "La nueva contraseña no puede estar vacía") @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres") @Schema(description = "Nueva contraseña (8-128 caracteres, debe incluir mayúsculas, minúsculas y números)", example = "NewSecurePass456") String passwordNueva) {
}