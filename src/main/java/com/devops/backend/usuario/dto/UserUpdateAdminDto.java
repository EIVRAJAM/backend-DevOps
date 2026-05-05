package com.devops.backend.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Datos para actualizar todos los atributos de un usuario como administrador")
public record UserUpdateAdminDto(
                @NotNull(message = "El rol no puede estar vacío") @Schema(description = "ID del rol a asignar al usuario", example = "2") Long idRol,

                @NotBlank(message = "El documento no puede estar vacío") @Size(max = 150, message = "El documento no puede exceder 150 caracteres") @Schema(description = "Número de documento único del usuario", example = "1234567890") String documento,

                @NotBlank(message = "Los nombres no pueden estar vacíos") @Size(max = 150, message = "Los nombres no pueden exceder 150 caracteres") @Schema(description = "Nombres del usuario", example = "Juan Carlos") String nombres,

                @NotBlank(message = "Los apellidos no pueden estar vacíos") @Size(max = 150, message = "Los apellidos no pueden exceder 150 caracteres") @Schema(description = "Apellidos del usuario", example = "Pérez García") String apellidos,

                @NotBlank(message = "Genero no puede estar vacío") @Pattern(regexp = "masculino|femenino", message = "El género debe ser masculino o femenino") @Schema(description = "Género (masculino o femenino)", example = "masculino") String genero,

                @Schema(description = "Fecha de nacimiento (ISO-8601)", example = "1990-05-15") LocalDate fechaNacimiento,

                @Size(max = 50, message = "El teléfono no puede exceder 50 caracteres") @Schema(description = "Número de teléfono de contacto", example = "+57 3001234567") String telefono

) {
}
