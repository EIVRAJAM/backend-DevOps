package com.devops.backend.acceso.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información completa de acceso del usuario (vista administrativa con campos sensibles)")
public record   AccesoAdminDTO(
                @Schema(description = "ID único del usuario propietario de este acceso", example = "123") Long idUsuario,

                @Schema(description = "Nombre de usuario para inicio de sesión", example = "jperez") String username,

                @Schema(description = "Correo electrónico registrado", example = "jperez@example.com") String correoAcceso,

                @Schema(description = "Contador de intentos fallidos de login (se reinicia al login exitoso)", example = "0") Integer intentosFallidos,

                @Schema(description = "Estado actual de la cuenta (ACTIVA, INACTIVA, BLOQUEADA)", example = "ACTIVA") String estadoCuenta,

                @Schema(description = "UUID único para identificación de auditoría", example = "550e8400-e29b-41d4-a716-446655440000") UUID uuidAcceso,

                @Schema(description = "Fecha y hora del último login exitoso (ISO-8601, null si nunca loginó)", example = "2026-04-18T14:30:00") LocalDateTime ultimoLogin,

                @Schema(description = "Fecha y hora de creación de la cuenta (ISO-8601)", example = "2026-01-15T10:00:00") LocalDateTime creadoEn,

                @Schema(description = "Fecha y hora de la última actualización de datos (ISO-8601)", example = "2026-04-18T14:30:00") LocalDateTime actualizadoEn) {
}