package com.devops.backend.pago.service;

import com.devops.backend.evento.entity.Ticket;
import com.devops.backend.evento.enums.EstadoTicket;
import com.devops.backend.evento.repository.TicketRepository;
import com.devops.backend.evento.service.TicketService;
import com.devops.backend.pago.entity.Pago;
import com.devops.backend.pago.repository.PagoRepository;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;

    @Override
    public void procesarPagoExitoso(Event event) {
        // Control de Idempotencia: si ya procesamos este evento, lo ignoramos
        if (pagoRepository.existsByStripeEventId(event.getId())) {
            return;
        }

        PaymentIntent paymentIntent = extractPaymentIntent(event);
        if (paymentIntent == null) return;

        Ticket ticket = findTicketFromMetadata(paymentIntent);
        if (ticket == null) return;

        // Actualizar Ticket
        ticket.setEstadoTicket(EstadoTicket.PAGADO);
        ticket.setMontoPagado(new BigDecimal(paymentIntent.getAmount()).divide(new BigDecimal("100")));
        ticketRepository.save(ticket);

        // Decrementar cupo disponible del evento de forma atómica
        ticketService.confirmarCupoTrasExitoso(ticket.getEvento().getIdEvento());

        // Guardar registro del Pago
        guardarPago(event, ticket, paymentIntent, "COBRO", "EXITOSO");
    }

    @Override
    public void procesarPagoFallido(Event event) {
        if (pagoRepository.existsByStripeEventId(event.getId())) {
            return;
        }

        PaymentIntent paymentIntent = extractPaymentIntent(event);
        if (paymentIntent == null) return;

        Ticket ticket = findTicketFromMetadata(paymentIntent);
        if (ticket == null) return;

        ticket.setEstadoTicket(EstadoTicket.CANCELADO);
        ticketRepository.save(ticket);

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
        if (paymentIntent.getMetadata() == null) return null;
        String ticketIdStr = paymentIntent.getMetadata().get("id_ticket");
        if (ticketIdStr == null) return null;

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
}
