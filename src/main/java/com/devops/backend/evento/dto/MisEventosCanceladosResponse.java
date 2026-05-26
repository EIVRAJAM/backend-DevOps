package com.devops.backend.evento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Lista de tickets de eventos cancelados con total")
public record MisEventosCanceladosResponse(
        @Schema(description = "Cantidad total de tickets", example = "1") int total,
        @Schema(description = "Lista de tickets de eventos cancelados") List<TicketEventoCanceladoResponse> tickets
) {
}
