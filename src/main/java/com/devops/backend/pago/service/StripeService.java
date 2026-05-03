package com.devops.backend.pago.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;

import java.math.BigDecimal;

public interface StripeService {

    /**
     * Crea un PaymentIntent en Stripe
     */
    PaymentIntent createPaymentIntent(BigDecimal amount, String currency, String receiptEmail, Long idTicket, Long idEvento, Long idUsuario) throws StripeException;

    /**
     * Verifica la firma del webhook y construye el objeto Event
     */
    Event constructEvent(String payload, String sigHeader);
}
