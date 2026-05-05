package com.devops.backend.evento.service;

import com.devops.backend.evento.dto.InscripcionTicketResponseDTO;
import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.entity.Ticket;
import com.devops.backend.evento.enums.EstadoTicket;
import com.devops.backend.evento.exception.EventoNoEncontradoException;
import com.devops.backend.evento.exception.TicketDuplicadoException;
import com.devops.backend.evento.repository.EventoRepository;
import com.devops.backend.evento.repository.TicketRepository;
import com.devops.backend.exception.BadRequestException;
import com.devops.backend.exception.ResourceNotFoundException;
import com.devops.backend.pago.service.StripeService;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.repository.UsuarioRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
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
    private final StripeService stripeService;

    @Override
    public InscripcionTicketResponseDTO inscribirseAEvento(Long eventoId, Long userId) {

        // Consulta única del evento — se reutiliza en los métodos internos
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNoEncontradoException(eventoId));

        if (esEventoGratis(evento)) {
            return procesarInscripcionGratis(evento, userId);
        } else {
            return procesarInscripcionPago(evento, userId);
        }
    }

    private InscripcionTicketResponseDTO procesarInscripcionPago(Evento evento, Long userId) {
        Usuario usuario = obtenerUsuarioPorId(userId);

        if (ticketRepository.existsByUsuario_IdUsuarioAndEvento_IdEvento(userId, evento.getIdEvento())) {
            throw new TicketDuplicadoException(userId, evento.getIdEvento());
        }

        // 1. Guardar el ticket inicialmente como PENDIENTE sin payment intent (para
        // obtener el idTicket generado por la base de datos)
        Ticket ticket = new Ticket();
        ticket.setEvento(evento);
        ticket.setUsuario(usuario);
        ticket.setEstadoTicket(EstadoTicket.PENDIENTE);
        ticket.setMontoPagado(BigDecimal.ZERO); // Aún no ha pagado
        ticket.setMoneda(evento.getMoneda());
        ticket.setCodigoQr(UUID.randomUUID().toString());
        ticket.setFechaCompra(LocalDateTime.now());

        Ticket ticketGuardado;
        try {
            ticketGuardado = ticketRepository.saveAndFlush(ticket);
        } catch (DataIntegrityViolationException e) {
            throw new TicketDuplicadoException(userId, evento.getIdEvento());
        }

        // 2. Crear PaymentIntent en Stripe
        try {
            String email = usuario.getAcceso() != null ? usuario.getAcceso().getCorreoAcceso() : null;
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                    evento.getPrecio(),
                    evento.getMoneda().name(),
                    email,
                    ticketGuardado.getIdTicket(),
                    evento.getIdEvento(),
                    userId);

            // 3. Actualizar el ticket con el Payment Intent ID
            ticketGuardado.setStripePaymentIntentId(paymentIntent.getId());
            ticketRepository.save(ticketGuardado);

            return new InscripcionTicketResponseDTO(
                    ticketGuardado.getIdTicket(),
                    ticketGuardado.getEstadoTicket(),
                    ticketGuardado.getCodigoQr(),
                    paymentIntent.getClientSecret());

        } catch (StripeException e) {
            // El RuntimeException genera un rollback automático de toda la transacción (no
            // se guarda el ticket basura en DB)
            throw new BadRequestException("Error al comunicarse con la pasarela de pagos (Stripe): " + e.getMessage());
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
        ticket.setMoneda(null); // Evento gratuito no requiere moneda
        ticket.setCodigoQr(UUID.randomUUID().toString()); // QR único basado en UUID
        ticket.setFechaCompra(LocalDateTime.now());
        return ticket;
    }

    private InscripcionTicketResponseDTO mapearRespuesta(Ticket ticket) {
        return new InscripcionTicketResponseDTO(
                ticket.getIdTicket(),
                ticket.getEstadoTicket(),
                ticket.getCodigoQr(),
                null // Eventos gratis no necesitan client_secret
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
