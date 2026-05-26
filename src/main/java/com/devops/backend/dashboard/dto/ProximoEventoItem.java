package com.devops.backend.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Proximo evento para el widget de dashboard")
public record ProximoEventoItem(
        @Schema(description = "ID del evento", example = "5") Long idEvento,
        @Schema(description = "Nombre del evento", example = "Conferencia DevOps") String nombreEvento,
        @Schema(description = "Fecha del evento (ISO-8601)", example = "2026-06-01") String fechaEvento
) {
}
