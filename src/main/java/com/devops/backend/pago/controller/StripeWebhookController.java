package com.devops.backend.pago.controller;

import com.devops.backend.pago.service.PagoService;
import com.devops.backend.pago.service.StripeService;
import com.stripe.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeService stripeService;
    private final PagoService pagoService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        // 1. Validar la firma y construir el evento de Stripe
        Event event = stripeService.constructEvent(payload, sigHeader);

        // 2. Manejar los eventos clave de la intención de pago
        switch (event.getType()) {
            case "payment_intent.succeeded":
                pagoService.procesarPagoExitoso(event);
                break;
            case "payment_intent.payment_failed":
                pagoService.procesarPagoFallido(event);
                break;
            default:
                // Ignoramos otros eventos de forma segura
                break;
        }

        // 3. Confirmar la recepción a Stripe
        return ResponseEntity.ok("Webhook recibido y procesado");
    }
}
