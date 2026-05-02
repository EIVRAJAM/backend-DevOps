package com.devops.backend.evento.dto;

import com.devops.backend.evento.enums.EstadoTicket;

public record InscripcionTicketResponseDTO(
        Long ticketId,
        EstadoTicket estado,
        String codigoQr
) {}
