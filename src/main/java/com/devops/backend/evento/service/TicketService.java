package com.devops.backend.evento.service;

import com.devops.backend.evento.dto.InscripcionTicketResponseDTO;
import com.devops.backend.evento.dto.MiEstadoInscripcionResponseDTO;
import com.devops.backend.evento.dto.TicketCheckoutResponseDTO;
import com.devops.backend.evento.dto.TicketResponseDTO;

import java.util.List;

public interface TicketService {

    /** Inscribir al usuario autenticado a un evento. */
    InscripcionTicketResponseDTO inscribirseAEvento(Long eventoId, Long userId);

    /** Devuelve todos los tickets del usuario autenticado. */
    List<TicketResponseDTO> obtenerMisTickets(Long userId);

    /** Devuelve el detalle de un ticket. */
    TicketResponseDTO obtenerTicketPorId(Long ticketId, Long userId);

    /** Devuelve todos los tickets de un evento para creador/admin. */
    List<TicketResponseDTO> obtenerTicketsPorEvento(Long eventoId, Long userId);

    /** Cancela un ticket activo del usuario autenticado. */
    TicketResponseDTO cancelarTicket(Long ticketId, Long userId);

    /** Llamado por el webhook de Stripe para decrementar cupo al confirmar pago. */
    void confirmarCupoTrasExitoso(Long eventoId);

    /** Genera la imagen PNG del QR del ticket. */
    byte[] generarQrTicket(Long ticketId, Long userId);

    /** Devuelve el checkout pendiente vigente del usuario para un evento. */
    TicketCheckoutResponseDTO obtenerCheckoutPendiente(Long eventoId, Long userId);

    /** Devuelve el estado de inscripcion del usuario autenticado para un evento. */
    MiEstadoInscripcionResponseDTO obtenerMiEstadoInscripcion(Long eventoId, Long userId);
}
