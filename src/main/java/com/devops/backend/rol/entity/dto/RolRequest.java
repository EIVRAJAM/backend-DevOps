package com.devops.backend.rol.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RolRequest(

        @NotBlank(message = "El nombre del rol es obligatorio")
        @Size(max = 100, message = "El nombre del rol no puede superar los 100 caracteres")
        @Pattern(
                regexp = "^ROLE_[A-Z]+$",
                message = "El nombre del rol debe tener el formato ROLE_EJEMPLO "
        )
        String nombreRol



) {

    public RolRequest {
        if (nombreRol != null) {
            nombreRol = nombreRol.trim().toUpperCase();
        }
    }
}
