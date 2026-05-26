package com.devops.backend.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuario reciente para el widget de dashboard")
public record UltimoUsuarioItem(
        @Schema(description = "ID del usuario", example = "10") Long idUsuario,
        @Schema(description = "Nombres del usuario", example = "Juan") String nombres,
        @Schema(description = "Apellidos del usuario", example = "Perez") String apellidos,
        @Schema(description = "Fecha de creacion (ISO-8601)", example = "2026-05-24") String fechaCreacion
) {
}
