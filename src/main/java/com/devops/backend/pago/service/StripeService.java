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
     * Consulta un PaymentIntent existente en Stripe.
     */
    PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException;

    /**
     * Cancela un PaymentIntent pendiente en Stripe cuando el checkout local expira.
     */
    PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException;

    /**
     * Verifica la firma del webhook y construye el objeto Event
     */
    Event constructEvent(String payload, String sigHeader);
    
    /**
     * Procesa un reembolso en Stripe
     */
    com.stripe.model.Refund createRefund(String chargeId, BigDecimal amount) throws StripeException;
}
