package com.devops.backend.pago.service;

import com.devops.backend.evento.entity.Ticket;
import com.devops.backend.evento.enums.EstadoTicket;
import com.devops.backend.evento.repository.TicketRepository;
import com.devops.backend.evento.service.TicketService;
import com.devops.backend.exception.ResourceNotFoundException;
import com.devops.backend.exception.UnauthorizedException;
import com.devops.backend.evento.repository.EventoRepository;
import com.devops.backend.pago.dto.PagoResponse;
import com.devops.backend.pago.entity.Pago;
import com.devops.backend.pago.repository.PagoRepository;
import com.devops.backend.shared.events.InscripcionConfirmadaEvent;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final EventoRepository eventoRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void procesarPagoExitoso(Event event) {
        // Control de Idempotencia: si ya procesamos este evento, lo ignoramos
        if (pagoRepository.existsByStripeEventId(event.getId())) {
            return;
        }

        PaymentIntent paymentIntent = extractPaymentIntent(event);
        if (paymentIntent == null)
            return;

        Ticket ticket = findTicketFromMetadata(paymentIntent);
        if (ticket == null)
            return;

        if (ticket.getEstadoTicket() != EstadoTicket.PENDIENTE) {
            guardarPago(event, ticket, paymentIntent, "COBRO", "EXITOSO");
            return;
        }

        // Actualizar Ticket
        ticket.setEstadoTicket(EstadoTicket.PAGADO);
        ticket.setMontoPagado(new BigDecimal(paymentIntent.getAmount()).divide(new BigDecimal("100")));
        ticketRepository.save(ticket);

        // Decrementar cupo disponible del evento de forma atómica
        ticketService.confirmarCupoTrasExitoso(ticket.getEvento().getIdEvento());

        String email = ticket.getUsuario().getAcceso() != null
                ? ticket.getUsuario().getAcceso().getCorreoAcceso()
                : null;

        //Mandamos el correo de confirmacion
        if (email != null) {
            eventPublisher.publishEvent(
                    new InscripcionConfirmadaEvent(this, ticket, ticket.getEvento(), email)
            );
        }
        // Guardar registro del Pago
        guardarPago(event, ticket, paymentIntent, "COBRO", "EXITOSO");

    }

    @Override
    public void procesarPagoFallido(Event event) {
        if (pagoRepository.existsByStripeEventId(event.getId())) {
            return;
        }

        PaymentIntent paymentIntent = extractPaymentIntent(event);
        if (paymentIntent == null)
            return;

        Ticket ticket = findTicketFromMetadata(paymentIntent);
        if (ticket == null)
            return;

        if (ticket.getEstadoTicket() == EstadoTicket.PENDIENTE) {
            ticket.setEstadoTicket(EstadoTicket.CANCELADO);
            ticketRepository.save(ticket);
        }

        guardarPago(event, ticket, paymentIntent, "COBRO", "FALLIDO");
    }

    private PaymentIntent extractPaymentIntent(Event event) {
        Optional<com.stripe.model.StripeObject> stripeObject = event.getDataObjectDeserializer().getObject();
        if (stripeObject.isPresent() && stripeObject.get() instanceof PaymentIntent) {
            return (PaymentIntent) stripeObject.get();
        }
        return null;
    }

    private Ticket findTicketFromMetadata(PaymentIntent paymentIntent) {
        if (paymentIntent.getMetadata() == null)
            return null;
        String ticketIdStr = paymentIntent.getMetadata().get("id_ticket");
        if (ticketIdStr == null)
            return null;

        Long idTicket = Long.parseLong(ticketIdStr);
        return ticketRepository.findById(idTicket).orElse(null);
    }

    private void guardarPago(Event event, Ticket ticket, PaymentIntent paymentIntent, String tipo, String estado) {
        Pago pago = new Pago();
        pago.setTicket(ticket);
        pago.setStripeChargeId(paymentIntent.getLatestCharge());
        pago.setStripeEventId(event.getId());
        pago.setMonto(new BigDecimal(paymentIntent.getAmount()).divide(new BigDecimal("100")));
        pago.setMoneda(paymentIntent.getCurrency().toUpperCase());
        pago.setTipoPago(tipo);
        pago.setEstadoPago(estado);
        pago.setStripeRawEvent(event.toJson());
        pagoRepository.save(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerPagosPorTicket(Long ticketId, Long userId, boolean isAdmin) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado"));

        boolean isOwner = ticket.getUsuario().getIdUsuario().equals(userId);
        boolean isOrganizer = isOrganizerOfEvent(ticket.getEvento().getIdEvento(), userId);

        if (!isAdmin && !isOwner && !isOrganizer) {
            throw new UnauthorizedException("No tienes permiso para ver pagos de este ticket");
        }

        return pagoRepository.findByTicket_IdTicket(ticketId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerMisPagos(Long userId) {
        return pagoRepository.findByTicket_Usuario_IdUsuario(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerPagosPorEvento(Long eventoId, Long userId, boolean isAdmin) {
        if (!isAdmin && !isOrganizerOfEvent(eventoId, userId)) {
            throw new UnauthorizedException("No tienes permiso para ver los pagos de este evento");
        }
        return pagoRepository.findByTicket_Evento_IdEvento(eventoId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private boolean isOrganizerOfEvent(Long idEvento, Long idUsuario) {
        return eventoRepository.findById(idEvento)
                .map(evento -> evento.getUsuarioCreador().getIdUsuario().equals(idUsuario))
                .orElse(false);
    }

    private PagoResponse mapToResponse(Pago p) {
        return com.devops.backend.pago.dto.PagoResponse.builder()
                .idPago(p.getIdPago())
                .idTicket(p.getTicket().getIdTicket())
                .stripeChargeId(p.getStripeChargeId())
                .stripeRefundId(p.getStripeRefundId())
                .monto(p.getMonto())
                .moneda(p.getMoneda())
                .tipoPago(p.getTipoPago())
                .estadoPago(p.getEstadoPago())
                .creadoEn(p.getCreadoEn())
                .build();
    }
}
