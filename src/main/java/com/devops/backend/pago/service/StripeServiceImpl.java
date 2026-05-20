package com.devops.backend.pago.service;

import com.devops.backend.exception.BadRequestException;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public PaymentIntent createPaymentIntent(BigDecimal amount, String currency, String receiptEmail, Long idTicket, Long idEvento, Long idUsuario) throws StripeException {
        // Multiplicamos por 100 para convertir a la unidad más pequeña requerida por Stripe (ej: centavos)
        long amountInSmallestUnit = amount.multiply(new BigDecimal("100")).longValue();

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amountInSmallestUnit)
                        .setCurrency(currency.toLowerCase())
                        .setReceiptEmail(receiptEmail)
                        .putMetadata("id_ticket", String.valueOf(idTicket))
                        .putMetadata("id_evento", String.valueOf(idEvento))
                        .putMetadata("id_usuario", String.valueOf(idUsuario))
                        .build();

        return PaymentIntent.create(params);
    }

    @Override
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }

    @Override
    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId).cancel();
    }

    @Override
    public Event constructEvent(String payload, String sigHeader) {
        try {
            return Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            throw new BadRequestException("Firma del webhook inválida de Stripe");
        } catch (Exception e) {
            throw new BadRequestException("Error inesperado al procesar el webhook de Stripe");
        }
    }

    @Override
    public com.stripe.model.Refund createRefund(String chargeId, BigDecimal amount) throws StripeException {
        long amountInSmallestUnit = amount.multiply(new BigDecimal("100")).longValue();
        
        com.stripe.param.RefundCreateParams params = com.stripe.param.RefundCreateParams.builder()
                .setCharge(chargeId)
                .setAmount(amountInSmallestUnit)
                .build();
                
        return com.stripe.model.Refund.create(params);
    }
}
