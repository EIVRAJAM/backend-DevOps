package com.devops.backend.evento.dto;

import com.devops.backend.evento.enums.EstadoTicket;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InscripcionTicketResponseDTO(
        Long ticketId,
        EstadoTicket estado,
        String codigoQr,
        String clientSecret
) {}
