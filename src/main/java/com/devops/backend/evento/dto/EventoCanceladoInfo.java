package com.devops.backend.evento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informacion resumida del evento cancelado dentro de la respuesta de tickets")
public record EventoCanceladoInfo(
        @Schema(description = "ID del evento", example = "7") Long idEvento,
        @Schema(description = "Nombre del evento", example = "Conferencia DevOps") String nombreEvento,
        @Schema(description = "Estado del evento", example = "CANCELADO") String estadoEvento,
        @Schema(description = "Fecha del evento (ISO-8601)", example = "2026-06-06") String fechaEvento,
        @Schema(description = "Hora del evento", example = "10:00:00") String horaEvento,
        @Schema(description = "Lugar del evento", example = "Universidad del Magdalena") String lugarEvento
) {
}
