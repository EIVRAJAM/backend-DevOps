package com.devops.backend.evento.service;

import com.devops.backend.evento.dto.InscripcionTicketResponseDTO;

public interface TicketService {
    InscripcionTicketResponseDTO inscribirseAEvento(Long eventoId, Long userId);
}
