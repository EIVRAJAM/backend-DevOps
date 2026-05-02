package com.devops.backend.evento.service;

import com.devops.backend.evento.dto.InscripcionTicketResponseDTO;
import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.entity.Ticket;
import com.devops.backend.evento.enums.EstadoTicket;
import com.devops.backend.evento.exception.EventoNoEncontradoException;
import com.devops.backend.evento.exception.EventoNoGratisException;
import com.devops.backend.evento.exception.TicketDuplicadoException;
import com.devops.backend.evento.repository.EventoRepository;
import com.devops.backend.evento.repository.TicketRepository;
import com.devops.backend.exception.ResourceNotFoundException;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public InscripcionTicketResponseDTO inscribirseAEvento(Long eventoId, Long userId) {

        // Consulta única del evento — se reutiliza en los métodos internos
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNoEncontradoException(eventoId));

        if (esEventoGratis(evento)) {
            return procesarInscripcionGratis(evento, userId);
        } else {
            /*
             * TODO: Integración con pasarela de pagos (ej. Stripe)
             * ─────────────────────────────────────────────────────────────────
             * Aquí se debe iniciar el proceso de pago cuando el evento es de pago.
             * Pasos futuros sugeridos:
             *   1. Obtener o crear un Customer en Stripe para el usuario.
             *   2. Crear un PaymentIntent con el monto del evento (evento.getPrecio()).
             *   3. Guardar el ticket en estado PENDIENTE con el stripePaymentIntentId.
             *   4. Retornar al frontend el client_secret para confirmar el pago.
             *   5. Confirmar el pago mediante webhook de Stripe y actualizar a PAGADO.
             * ─────────────────────────────────────────────────────────────────
             */
            throw new EventoNoGratisException(eventoId);
        }
    }

    private InscripcionTicketResponseDTO procesarInscripcionGratis(Evento evento, Long userId) {

        Usuario usuario = obtenerUsuarioPorId(userId);

        // Validación previa a la inserción para dar un error semántico claro
        if (ticketRepository.existsByUsuario_IdUsuarioAndEvento_IdEvento(userId, evento.getIdEvento())) {
            throw new TicketDuplicadoException(userId, evento.getIdEvento());
        }

        Ticket ticket = construirTicketGratis(evento, usuario);

        try {
            Ticket ticketGuardado = ticketRepository.save(ticket);
            return mapearRespuesta(ticketGuardado);
        } catch (DataIntegrityViolationException e) {
            // Segunda barrera: captura el constraint UNIQUE (uq_tickets_usuario_evento)
            // ante una posible condición de carrera entre hilos concurrentes
            throw new TicketDuplicadoException(userId, evento.getIdEvento());
        }
    }

    private Ticket construirTicketGratis(Evento evento, Usuario usuario) {
        Ticket ticket = new Ticket();
        ticket.setEvento(evento);
        ticket.setUsuario(usuario);
        ticket.setEstadoTicket(EstadoTicket.GRATIS);
        ticket.setMontoPagado(BigDecimal.ZERO);
        ticket.setMoneda(null);                            // Evento gratuito no requiere moneda
        ticket.setCodigoQr(UUID.randomUUID().toString());  // QR único basado en UUID
        ticket.setFechaCompra(LocalDateTime.now());
        return ticket;
    }

   
    private InscripcionTicketResponseDTO mapearRespuesta(Ticket ticket) {
        return new InscripcionTicketResponseDTO(
                ticket.getIdTicket(),
                ticket.getEstadoTicket(),
                ticket.getCodigoQr()
        );
    }


    private Usuario obtenerUsuarioPorId(Long userId) {
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario con ID " + userId + " no encontrado"));
    }

    private boolean esEventoGratis(Evento evento) {
        boolean noEsDePago = !Boolean.TRUE.equals(evento.getEsDePago());
        boolean sinPrecio = evento.getPrecio() == null
                || evento.getPrecio().compareTo(BigDecimal.ZERO) == 0;
        return noEsDePago && sinPrecio;
    }
}
