package com.devops.backend.auth.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;

public record SignUpRequest(
        @NotBlank @Schema(description = "Número de documento de identificación del usuario", example = "1234567890") String documento,

        @NotBlank @Schema(description = "Nombres del usuario", example = "Juan") String nombres,

        @NotBlank @Schema(description = "Apellidos del usuario", example = "Pérez García") String apellidos,

        @Pattern(regexp = "masculino|femenino", message = "El género debe ser masculino o femenino") @Schema(description = "Género del usuario (masculino o femenino)", example = "masculino") String genero,

        @Past @Schema(description = "Fecha de nacimiento (formato: 1990-05-15)", example = "1990-05-15") Date fechaNacimiento,

        @NotBlank @Schema(description = "Número de teléfono de contacto", example = "+57 3001234567") String telefono,

        @NotBlank @Schema(description = "Nombre de usuario único para inicio de sesión", example = "jperez") String username,

        @Email @Schema(description = "Correo electrónico de acceso (debe ser válido)", example = "jperez@example.com") String correoAcceso,

        @NotBlank @Size(min = 8, message = "La clave debe tener al menos 8 caracteres") @Schema(description = "Contraseña (mínimo 8 caracteres, debe incluir mayúsculas, minúsculas y números)", example = "SecurePass123") String claveAcceso) {
}
