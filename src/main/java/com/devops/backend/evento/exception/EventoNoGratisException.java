package com.devops.backend.evento.exception;

/**
 * Excepción lanzada cuando se intenta usar el flujo de inscripción gratuita
 * en un evento que es de pago.
 *
 * <p>HTTP 400 Bad Request.</p>
 */
public class EventoNoGratisException extends RuntimeException {

    public EventoNoGratisException(Long idEvento) {
        super("El evento con ID " + idEvento + " es de pago. Se requiere proceso de pago para inscribirse.");
    }

    public EventoNoGratisException(String message) {
        super(message);
    }
}
