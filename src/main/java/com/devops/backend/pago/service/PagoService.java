package com.devops.backend.pago.service;

import com.devops.backend.pago.dto.PagoResponse;
import com.stripe.model.Event;
import java.util.List;

public interface PagoService {
    /**
     * Procesa un webhook de pago exitoso (payment_intent.succeeded)
     */
    void procesarPagoExitoso(Event event);

    /**
     * Procesa un webhook de pago fallido (payment_intent.payment_failed)
     */
    void procesarPagoFallido(Event event);

    List<PagoResponse> obtenerPagosPorTicket(Long ticketId, Long userId, boolean isAdmin);
    List<PagoResponse> obtenerMisPagos(Long userId);
    List<PagoResponse> obtenerPagosPorEvento(Long eventoId, Long userId, boolean isAdmin);
}
