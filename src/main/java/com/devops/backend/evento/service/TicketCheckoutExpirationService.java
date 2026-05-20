package com.devops.backend.evento.service;

import com.devops.backend.evento.entity.Ticket;
import com.devops.backend.evento.enums.EstadoTicket;
import com.devops.backend.evento.repository.TicketRepository;
import com.devops.backend.pago.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketCheckoutExpirationService {

    private static final String STRIPE_STATUS_SUCCEEDED = "succeeded";
    private static final String STRIPE_STATUS_CANCELED = "canceled";
    private static final String STRIPE_STATUS_PROCESSING = "processing";

    private final TicketRepository ticketRepository;
    private final StripeService stripeService;

    @Transactional
    public void resolverCheckoutPendienteVencido(Long userId, Long eventoId) {
        ticketRepository
                .findFirstByUsuario_IdUsuarioAndEvento_IdEventoAndEstadoTicketOrderByFechaCompraDesc(
                        userId,
                        eventoId,
                        EstadoTicket.PENDIENTE)
                .filter(this::estaVencido)
                .ifPresent(this::resolverTicketPendienteVencido);
    }

    @Transactional
    public int resolverTicketsPendientesVencidos(int limite) {
        List<Ticket> tickets = ticketRepository.findTicketsPendientesVencidos(
                LocalDateTime.now(),
                EstadoTicket.PENDIENTE,
                PageRequest.of(0, limite));

        int resueltos = 0;
        for (Ticket ticket : tickets) {
            if (resolverTicketPendienteVencido(ticket)) {
                resueltos++;
            }
        }

        return resueltos;
    }

    private boolean resolverTicketPendienteVencido(Ticket ticket) {
        if (ticket.getEstadoTicket() != EstadoTicket.PENDIENTE || !estaVencido(ticket)) {
            return false;
        }

        String paymentIntentId = ticket.getStripePaymentIntentId();
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            marcarExpirado(ticket);
            return true;
        }

        try {
            PaymentIntent paymentIntent = stripeService.retrievePaymentIntent(paymentIntentId);
            String stripeStatus = paymentIntent.getStatus();

            if (STRIPE_STATUS_SUCCEEDED.equals(stripeStatus)) {
                marcarPagado(ticket, paymentIntent);
                return true;
            }

            if (STRIPE_STATUS_PROCESSING.equals(stripeStatus)) {
                log.info("Checkout pendiente aun procesando en Stripe. ticket={}, intent={}",
                        ticket.getIdTicket(), paymentIntentId);
                return false;
            }

            if (!STRIPE_STATUS_CANCELED.equals(stripeStatus)) {
                stripeService.cancelPaymentIntent(paymentIntentId);
            }

            marcarExpirado(ticket);
            return true;
        } catch (StripeException e) {
            log.warn("No se pudo verificar checkout vencido en Stripe. ticket={}, intent={}, error={}",
                    ticket.getIdTicket(), paymentIntentId, e.getMessage());
            return false;
        }
    }

    private boolean estaVencido(Ticket ticket) {
        return ticket.getExpiraEn() != null && !ticket.getExpiraEn().isAfter(LocalDateTime.now());
    }

    private void marcarPagado(Ticket ticket, PaymentIntent paymentIntent) {
        ticket.setEstadoTicket(EstadoTicket.PAGADO);
        ticket.setMontoPagado(new BigDecimal(paymentIntent.getAmount()).divide(new BigDecimal("100")));
        ticketRepository.saveAndFlush(ticket);
        ticketRepository.decrementarCupo(ticket.getEvento().getIdEvento());
    }

    private void marcarExpirado(Ticket ticket) {
        ticket.setEstadoTicket(EstadoTicket.EXPIRADO);
        ticketRepository.saveAndFlush(ticket);
    }
}
