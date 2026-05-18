package com.devops.backend.evento.service;

import com.devops.backend.evento.dto.InscripcionTicketResponseDTO;
import com.devops.backend.evento.dto.TicketResponseDTO;

import java.util.List;

public interface TicketService {

    /** Inscribir al usuario autenticado a un evento */
    InscripcionTicketResponseDTO inscribirseAEvento(Long eventoId, Long userId);

    /** Devuelve todos los tickets del usuario autenticado */
    List<TicketResponseDTO> obtenerMisTickets(Long userId);

    /** Devuelve el detalle de un ticket (solo su propietario o admin) */
    TicketResponseDTO obtenerTicketPorId(Long ticketId, Long userId);

    /** Devuelve todos los tickets de un evento (solo creador o admin) */
    List<TicketResponseDTO> obtenerTicketsPorEvento(Long eventoId, Long userId);

    /** Cancela un ticket activo del usuario autenticado */
    TicketResponseDTO cancelarTicket(Long ticketId, Long userId);

    /** Llamado por el webhook de Stripe para decrementar el cupo al confirmar pago */
    void confirmarCupoTrasExitoso(Long eventoId);

    /** Genera la imagen PNG del QR del ticket (solo dueño o admin) */
    byte[] generarQrTicket(Long ticketId, Long userId);
}
