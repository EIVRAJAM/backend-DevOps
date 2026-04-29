package com.devops.backend.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Respuesta con todos los datos del usuario después de una actualización admin")
public record UserUpdateAdminResponse(
        @Schema(description = "ID único del usuario", example = "123") Long idUsuario,

        @Schema(description = "Número de documento de identificación", example = "1234567890") String documento,

        @Schema(description = "Nombres del usuario", example = "Juan Carlos") String nombres,

        @Schema(description = "Apellidos del usuario", example = "Pérez García") String apellidos,

        @Schema(description = "Género del usuario (1=Masculino, 2=Femenino)", example = "1") Short genero,

        @Schema(description = "Fecha de nacimiento (ISO-8601)", example = "1990-05-15") LocalDate fechaNacimiento,

        @Schema(description = "Número de teléfono de contacto", example = "+57 3001234567") String telefono,

        @Schema(description = "Estado del usuario (ACTIVO, INACTIVO, BLOQUEADO)", example = "ACTIVO") String estado,

        @Schema(description = "Nombre del rol asignado", example = "ROLE_ADMIN") String nombreRol,

        @Schema(description = "Fecha de creación del registro (ISO-8601)", example = "2024-01-15T10:30:00") LocalDateTime creadoEn,

        @Schema(description = "Fecha de última actualización (ISO-8601)", example = "2024-01-15T15:45:00") LocalDateTime actualizadoEn) {
}
