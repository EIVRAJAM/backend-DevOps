package com.devops.backend.pago.service;

import com.stripe.model.Event;

public interface PagoService {
    /**
     * Procesa un webhook de pago exitoso (payment_intent.succeeded)
     */
    void procesarPagoExitoso(Event event);

    /**
     * Procesa un webhook de pago fallido (payment_intent.payment_failed)
     */
    void procesarPagoFallido(Event event);
}
