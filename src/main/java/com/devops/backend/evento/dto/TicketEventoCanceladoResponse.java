package com.devops.backend.evento.dto;

import com.devops.backend.evento.enums.EstadoTicket;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ticket de un evento cancelado con informacion de reembolso")
public record TicketEventoCanceladoResponse(
        @Schema(description = "ID del ticket", example = "32") Long idTicket,
        @Schema(description = "Estado actual del ticket", example = "PAGADO") EstadoTicket estadoTicket,
        @Schema(description = "Informacion del evento cancelado") EventoCanceladoInfo evento,
        @Schema(description = "Indica si el reembolso esta disponible para este ticket", example = "true") boolean reembolsoDisponible,
        @Schema(description = "Estado del reembolso", example = "DISPONIBLE") String estadoReembolso,
        @Schema(description = "Mensaje descriptivo sobre la situacion del reembolso", example = "El evento fue cancelado. Puedes solicitar el reembolso.") String mensaje
) {
}
