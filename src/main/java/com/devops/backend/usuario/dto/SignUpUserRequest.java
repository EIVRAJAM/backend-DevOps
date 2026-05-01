package com.devops.backend.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.util.Date;

public record SignUpUserRequest(
        @NotBlank @Schema(description = "Número de documento de identificación del usuario", example = "1234567890") String documento,

        @NotBlank @Schema(description = "Nombres del usuario", example = "Juan") String nombres,

        @NotBlank @Schema(description = "Apellidos del usuario", example = "Pérez García") String apellidos,

        @Pattern(regexp = "masculino|femenino", message = "El género debe ser masculino o femenino") @Schema(description = "Género del usuario (masculino o femenino)", example = "masculino") String genero,

        @Past @Schema(description = "Fecha de nacimiento (formato: 1990-05-15)", example = "1990-05-15") Date fechaNacimiento,

        @NotBlank @Schema(description = "Número de teléfono de contacto", example = "+57 3001234567") String telefono



        ) {
}
