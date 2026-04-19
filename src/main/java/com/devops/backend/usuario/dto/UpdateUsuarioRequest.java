package com.devops.backend.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Date;

@Schema(description = "Datos para actualizar información de un usuario existente")
public record UpdateUsuarioRequest(
                @NotBlank(message = "El documento no puede estar vacío") @Size(max = 20) @Schema(description = "Número de documento (puede cambiar si no está registrado)", example = "1234567890") String documento,

                @NotBlank(message = "Los nombres no pueden estar vacíos") @Schema(description = "Nombres del usuario", example = "Juan Carlos") String nombres,

                @NotBlank(message = "Los apellidos no pueden estar vacíos") @Schema(description = "Apellidos del usuario", example = "Pérez García") String apellidos,

                @NotBlank(message = "El Telefono no puede estar vacío") @Schema(description = "Número de teléfono de contacto", example = "+57 3001234567") String telefono,

                @NotBlank(message = "Genero no puede estar vacío") @Pattern(regexp = "masculino|femenino", message = "El género debe ser masculino o femenino") @Schema(description = "Género (masculino o femenino)", example = "masculino") String genero,

                @NotNull(message = "Fecha de nacimiento no puede estar vacío") @Schema(description = "Fecha de nacimiento (ISO-8601)", example = "1990-05-15") Date fechaNacimiento,

                @NotNull(message = "El rol no puede estar vacío") @Schema(description = "ID del rol a asignar al usuario", example = "2") Long idRol) {
}
