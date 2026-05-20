package com.devops.backend.evento.dto;

import com.devops.backend.evento.enums.EstadoTicket;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Checkout pendiente vigente para continuar un pago")
public record TicketCheckoutResponseDTO(
        @Schema(description = "ID del ticket pendiente", example = "10")
        Long ticketId,

        @Schema(description = "ID del evento asociado", example = "3")
        Long eventoId,

        @Schema(description = "Nombre del evento", example = "DevOps Summit")
        String nombreEvento,

        @Schema(description = "Estado actual del ticket", example = "PENDIENTE")
        EstadoTicket estado,

        @Schema(description = "Client secret de Stripe para continuar el pago")
        String clientSecret,

        @Schema(description = "Fecha y hora de expiracion del checkout")
        LocalDateTime expiraEn
) {}
