package com.devops.backend.rol.dto;

import com.devops.backend.rol.enums.Estado;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Información requerida para actualizar un rol existente")
public record RolUpdateDto(

        @NotBlank
        @Pattern(
                regexp = "^ROLE_[A-Z]+$",
                message = "El nombre del rol debe tener el formato ROLE_EJEMPLO"
        )
        @Schema(
                description = "Nuevo nombre del rol en formato ROLE_NOMBRE (solo mayúsculas)",
                example = "ROLE_SUPERVISOR"
        )
        String nombreRol,

        @NotNull(message = "El estado no puede estar vacío")
        @Enumerated(EnumType.STRING)
        @Schema(
                description = "Nuevo estado del rol (ACTIVO o INACTIVO)",
                example = "ACTIVO"
        )
        Estado estado

) {
    public RolUpdateDto {
        if (nombreRol != null) {
            nombreRol = nombreRol.trim().toUpperCase();
        }
    }
}