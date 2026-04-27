package com.devops.backend.rol.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Información requerida para crear un nuevo rol en el sistema")
public record RolRequest(

        @NotBlank(message = "El nombre del rol es obligatorio")
        @Size(max = 100, message = "El nombre del rol no puede superar los 100 caracteres")
        @Pattern(
                regexp = "^ROLE_[A-Z]+$",
                message = "El nombre del rol debe tener el formato ROLE_EJEMPLO"
        )
        @Schema(
                description = "Nombre del rol en formato ROLE_NOMBRE (máximo 100 caracteres, solo mayúsculas)",
                example = "ROLE_ADMIN"
        )
        String nombreRol

) {
    public RolRequest {
        if (nombreRol != null) {
            nombreRol = nombreRol.trim().toUpperCase();
        }
    }
}