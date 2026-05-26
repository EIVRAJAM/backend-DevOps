package com.devops.backend.sesion.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de una sesión de usuario en el sistema")
public record SesionResponseDto(
                @Schema(description = "ID único de la sesión", example = "42") Long idSesion,

                @Schema(description = "ID del usuario propietario de la sesión", example = "5") Long idUsuario,

                @Schema(description = "Nombres del usuario", example = "Juan") String nombresUsuario,

                @Schema(description = "Apellidos del usuario", example = "Pérez García") String apellidosUsuario,

                @Schema(description = "Fecha y hora de inicio de la sesión (ISO-8601)", example = "2025-07-15T10:30:00") LocalDateTime fechaInicio,

                @Schema(description = "Fecha y hora de cierre/caducidad de la sesión (ISO-8601, null si aún activa)", example = "2025-07-16T10:30:00") LocalDateTime fechaFin,

                @Schema(description = "¿Sesión actualmente activa?", example = "true") Boolean activa,

                @Schema(description = "JTI (ID único del token JWT asociado)", example = "550e8400-e29b-41d4-a716-446655440000") String tokenJti) {
}
