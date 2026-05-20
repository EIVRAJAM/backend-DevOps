package com.devops.backend.evento.service;

import com.devops.backend.evento.dto.InscripcionTicketResponseDTO;
import com.devops.backend.evento.dto.TicketResponseDTO;
import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.entity.Ticket;
import com.devops.backend.evento.enums.Estado;
import com.devops.backend.evento.enums.EstadoEvento;
import com.devops.backend.evento.enums.EstadoTicket;
import com.devops.backend.evento.exception.CuposAgotadosException;
import com.devops.backend.evento.exception.EventoNoEncontradoException;
import com.devops.backend.evento.exception.EventoNoInscribibleException;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final List<EstadoTicket> ESTADOS_TICKET_ACTIVOS = List.of(
            EstadoTicket.PENDIENTE,
            EstadoTicket.PAGADO,
            EstadoTicket.GRATIS);

    private final TicketRepository ticketRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final StripeService stripeService;
    private final QrCodeService qrCodeService;

    // ─────────────────────────── INSCRIPCION ────────────────────────────────

    @Override
    public InscripcionTicketResponseDTO inscribirseAEvento(Long eventoId, Long userId) {

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNoEncontradoException(eventoId));

        // Validar estado: solo PUBLICADO + ACTIVO
        if (evento.getEstadoEvento() != EstadoEvento.PUBLICADO
                || evento.getEstado() != Estado.ACTIVO) {
            throw new EventoNoInscribibleException(
                    eventoId,
                    evento.getEstadoEvento().name(),
                    evento.getEstado().name());
        }

        if (esEventoGratis(evento)) {
            return procesarInscripcionGratis(evento, userId);
        } else {
            return procesarInscripcionPago(evento, userId);
        }
    }

    // ─────────────────────────── GRATIS ─────────────────────────────────────

    private InscripcionTicketResponseDTO procesarInscripcionGratis(Evento evento, Long userId) {

        Usuario usuario = obtenerUsuarioPorId(userId);

        // Validación previa — error semántico claro
        if (tieneTicketActivoParaEvento(userId, evento.getIdEvento())) {
            throw new TicketDuplicadoException(userId, evento.getIdEvento());
        }

        // Decrementar cupo de forma atómica (anti-sobreventa)
        int filasAfectadas = ticketRepository.decrementarCupo(evento.getIdEvento());
        if (filasAfectadas == 0) {
            throw new CuposAgotadosException(evento.getIdEvento());
        }

        Ticket ticket = construirTicketGratis(evento, usuario);

        try {
            Ticket ticketGuardado = ticketRepository.save(ticket);
            return mapearRespuesta(ticketGuardado);
        } catch (DataIntegrityViolationException e) {
            // Segunda barrera: constraint UNIQUE ante condición de carrera
            throw new TicketDuplicadoException(userId, evento.getIdEvento());
        }
    }

    private Ticket construirTicketGratis(Evento evento, Usuario usuario) {
        Ticket ticket = new Ticket();
        ticket.setEvento(evento);
        ticket.setUsuario(usuario);
        ticket.setEstadoTicket(EstadoTicket.GRATIS);
        ticket.setMontoPagado(BigDecimal.ZERO);
        ticket.setMoneda(null);
        ticket.setCodigoQr(UUID.randomUUID().toString());
        ticket.setFechaCompra(LocalDateTime.now());
        return ticket;
    }

    private InscripcionTicketResponseDTO mapearRespuesta(Ticket ticket) {
        return new InscripcionTicketResponseDTO(
                ticket.getIdTicket(),
                ticket.getEstadoTicket(),
                ticket.getCodigoQr(),
                null);
    }

    // ─────────────────────────── PAGO ────────────────────────────────────────

    private InscripcionTicketResponseDTO procesarInscripcionPago(Evento evento, Long userId) {
        Usuario usuario = obtenerUsuarioPorId(userId);

        if (tieneTicketActivoParaEvento(userId, evento.getIdEvento())) {
            throw new TicketDuplicadoException(userId, evento.getIdEvento());
        }

        // Validar cupos ANTES de crear el ticket (lectura rápida)
        if (evento.getCapacidadDisponible() == null || evento.getCapacidadDisponible() <= 0) {
            throw new CuposAgotadosException(evento.getIdEvento());
        }

        // Guardar ticket inicialmente como PENDIENTE (sin descontar cupo aún —
        // el descuento ocurre cuando el pago sea exitoso vía webhook)
        Ticket ticket = new Ticket();
        ticket.setEvento(evento);
        ticket.setUsuario(usuario);
        ticket.setEstadoTicket(EstadoTicket.PENDIENTE);
        ticket.setMontoPagado(BigDecimal.ZERO);
        ticket.setMoneda(evento.getMoneda());
        ticket.setCodigoQr(UUID.randomUUID().toString());
        ticket.setFechaCompra(LocalDateTime.now());

        Ticket ticketGuardado;
        try {
            ticketGuardado = ticketRepository.saveAndFlush(ticket);
        } catch (DataIntegrityViolationException e) {
            throw new TicketDuplicadoException(userId, evento.getIdEvento());
        }

        // Crear PaymentIntent en Stripe
        try {
            String email = usuario.getAcceso() != null ? usuario.getAcceso().getCorreoAcceso() : null;
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                    evento.getPrecio(),
                    evento.getMoneda().name(),
                    email,
                    ticketGuardado.getIdTicket(),
                    evento.getIdEvento(),
                    userId);

            ticketGuardado.setStripePaymentIntentId(paymentIntent.getId());
            ticketRepository.save(ticketGuardado);

            return new InscripcionTicketResponseDTO(
                    ticketGuardado.getIdTicket(),
                    ticketGuardado.getEstadoTicket(),
                    ticketGuardado.getCodigoQr(),
                    paymentIntent.getClientSecret());

        } catch (StripeException e) {
            throw new BadRequestException(
                    "Error al comunicarse con la pasarela de pagos (Stripe): " + e.getMessage());
        }
    }

    // ─────────────────────────── CUPO (webhook) ─────────────────────────────

    /**
     * Decrementa el cupo del evento de forma atómica cuando Stripe confirma el pago.
     * Si no quedan cupos (edge-case de concurrencia) el ticket ya está PAGADO
     * pero se registra el incidente — no se revierte el pago aquí.
     */
    @Override
    public void confirmarCupoTrasExitoso(Long eventoId) {
        ticketRepository.decrementarCupo(eventoId);
    }

    // ─────────────────────────── MIS TICKETS ────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> obtenerMisTickets(Long userId) {
        return ticketRepository
                .findByUsuario_IdUsuarioOrderByFechaCompraDesc(userId)
                .stream()
                .map(this::toTicketResponseDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────── TICKET POR ID ──────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public TicketResponseDTO obtenerTicketPorId(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket con ID " + ticketId + " no encontrado"));

        // Solo el propietario o admin pueden ver el ticket
        if (!ticket.getUsuario().getIdUsuario().equals(userId) && !tieneRolAdmin()) {
            throw new AccessDeniedException("No tienes permiso para ver este ticket");
        }

        return toTicketResponseDTO(ticket);
    }

    // ─────────────────────────── TICKETS POR EVENTO ─────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> obtenerTicketsPorEvento(Long eventoId, Long userId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNoEncontradoException(eventoId));

        // Solo el creador del evento o un admin pueden listar sus tickets
        if (!evento.getUsuarioCreador().getIdUsuario().equals(userId) && !tieneRolAdmin()) {
            throw new AccessDeniedException(
                    "Solo el creador del evento o un administrador puede ver sus tickets");
        }

        return ticketRepository
                .findByEvento_IdEventoOrderByFechaCompraDesc(eventoId)
                .stream()
                .map(this::toTicketResponseDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────── CANCELAR TICKET ────────────────────────────

    @Override
    public TicketResponseDTO cancelarTicket(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket con ID " + ticketId + " no encontrado"));

        // Solo el propietario puede cancelar su propio ticket
        if (!ticket.getUsuario().getIdUsuario().equals(userId)) {
            throw new AccessDeniedException("Solo puedes cancelar tus propios tickets");
        }

        if (ticket.getEstadoTicket() == EstadoTicket.CANCELADO
                || ticket.getEstadoTicket() == EstadoTicket.REEMBOLSADO) {
            throw new BadRequestException(
                    "El ticket ya se encuentra en estado " + ticket.getEstadoTicket());
        }

        boolean eraActivo = ticket.getEstadoTicket() == EstadoTicket.GRATIS
                || ticket.getEstadoTicket() == EstadoTicket.PAGADO;

        ticket.setEstadoTicket(EstadoTicket.CANCELADO);
        Ticket actualizado = ticketRepository.save(ticket);

        // Devolver el cupo si el ticket estaba activo (gratis o pagado)
        if (eraActivo) {
            eventoRepository.findById(ticket.getEvento().getIdEvento()).ifPresent(evento -> {
                int actual = evento.getCapacidadDisponible() != null ? evento.getCapacidadDisponible() : 0;
                evento.setCapacidadDisponible(actual + 1);
                eventoRepository.save(evento);
            });
        }

        return toTicketResponseDTO(actualizado);
    }

    // ─────────────────────────── UTILIDADES ─────────────────────────────────

    private boolean esEventoGratis(Evento evento) {
        boolean noEsDePago = !Boolean.TRUE.equals(evento.getEsDePago());
        boolean sinPrecio = evento.getPrecio() == null
                || evento.getPrecio().compareTo(BigDecimal.ZERO) == 0;
        return noEsDePago && sinPrecio;
    }

    private Usuario obtenerUsuarioPorId(Long userId) {
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario con ID " + userId + " no encontrado"));
    }

    private boolean tieneTicketActivoParaEvento(Long userId, Long eventoId) {
        return ticketRepository.existsByUsuario_IdUsuarioAndEvento_IdEventoAndEstadoTicketIn(
                userId,
                eventoId,
                ESTADOS_TICKET_ACTIVOS);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarQrTicket(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado"));

        // Solo el dueño del ticket o un admin pueden descargar el QR
        boolean esDuenio = ticket.getUsuario().getIdUsuario().equals(userId);
        if (!esDuenio && !tieneRolAdmin()) {
            throw new AccessDeniedException("No tienes permisos para ver el QR de este ticket");
        }

        String codigoQr = ticket.getCodigoQr();
        if (codigoQr == null || codigoQr.isBlank()) {
            throw new ResourceNotFoundException("Este ticket no tiene un código QR asignado");
        }

        return qrCodeService.generarQrPng(codigoQr);
    }

    private boolean tieneRolAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private TicketResponseDTO toTicketResponseDTO(Ticket ticket) {
        return new TicketResponseDTO(
                ticket.getIdTicket(),
                ticket.getEvento().getIdEvento(),
                ticket.getEvento().getNombreEvento(),
                ticket.getUsuario().getIdUsuario(),
                ticket.getEstadoTicket(),
                ticket.getMontoPagado(),
                ticket.getMoneda(),
                ticket.getCodigoQr(),
                ticket.getFechaCompra(),
                ticket.getCreadoEn());
    }
}
