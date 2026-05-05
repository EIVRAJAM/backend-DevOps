package com.devops.backend.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Date;

public record SignUpUserRequest(

        @NotBlank @Schema(description = "Número de documento de identificación del usuario", example = "1234567890") String documento,

        @NotBlank @Schema(description = "Nombres del usuario", example = "Juan") String nombres,

        @NotBlank @Schema(description = "Apellidos del usuario", example = "Pérez García") String apellidos,

        @NotBlank(message = "El género es obligatorio")
        @Pattern(regexp = "masculino|femenino", message = "El género debe ser masculino o femenino")
        @Schema(description = "Género del usuario (masculino o femenino)", example = "masculino")
        String genero,

        @Past @Schema(description = "Fecha de nacimiento (formato: 1990-05-15)", example = "1990-05-15") Date fechaNacimiento,

        @NotBlank @Schema(description = "Número de teléfono de contacto", example = "+57 3001234567") String telefono,

        @NotBlank(message = "El nombre del rol es obligatorio")
        @Size(max = 100, message = "El nombre del rol no puede superar los 100 caracteres")
        @Pattern(
                regexp = "^ROLE_[A-Z]+$",
                message = "El nombre del rol debe tener el formato ROLE_EJEMPLO"
        )
        @Schema(
                description = "Nombre del rol en formato ROLE_NOMBRE (máximo 100 caracteres, solo mayúsculas)",
                example = "ROLE_USER"
        )
        String nombreRol

        ) {

    public SignUpUserRequest {
        if (nombreRol != null) {
            nombreRol = nombreRol.trim().toUpperCase();
        }
    }
}
