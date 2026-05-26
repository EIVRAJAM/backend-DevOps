package com.devops.backend.sesion.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Criterios de filtro para búsqueda de sesiones")
public record SesionFilterRequest(
                @Schema(description = "Filtro opcional por ID de usuario", example = "5") Long idUsuario,

                @Schema(description = "Filtro opcional: fecha/hora mínima de inicio (ISO-8601). Busca sesiones que iniciaron en o después de esta fecha", example = "2025-07-01T00:00:00") LocalDateTime fechaInicio,

                @Schema(description = "Filtro opcional: fecha/hora máxima de fin. Busca sesiones que terminaron antes de esta fecha", example = "2025-07-31T23:59:59") LocalDateTime fechaFin,

                @Schema(description = "Filtro opcional por estado: true=solo activas, false=solo cerradas, null=ambas", example = "true") Boolean activa,

                @Schema(description = "Número de página (comienza en 0)", example = "0") int page,

                @Schema(description = "Cantidad de registros por página", example = "10") int size) {
}
