package com.devops.backend.acceso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para cambio de contraseña por administrador (sin validación de contraseña actual)")
public record ActualizarPasswordAdminDTO(
        @NotBlank(message = "La nueva contraseña no puede estar vacía") @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres") @Schema(description = "Nueva contraseña (8-128 caracteres, debe incluir mayúsculas, minúsculas y números). No requiere validación de contraseña actual (solo admin puede hacer esto).", example = "ResetPassword789") String passwordNueva) {
}