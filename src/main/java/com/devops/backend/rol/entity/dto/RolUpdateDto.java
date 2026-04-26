package com.devops.backend.rol.entity.dto;

import com.devops.backend.rol.enums.Estado;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RolUpdateDto(


        @NotBlank
        @Pattern(
                regexp = "^ROLE_[A-Z]+$",
                message = "El nombre del rol debe tener el formato ROLE_EJEMPLO "
        )
        String nombreRol,

        @NotNull(message = "El estado no puede estar vacío")
        @Enumerated(EnumType.STRING)
        Estado estado


) {
    public RolUpdateDto {
        if (nombreRol != null) {
            nombreRol = nombreRol.trim().toUpperCase();
        }
    }
}
