package com.devops.backend.evento.exception;

/**
 * Excepción lanzada cuando se intenta inscribir a un evento de pago
 * usando el flujo de inscripción gratuita.
 */
public class EventoNoBecarioException extends RuntimeException {

    public EventoNoBecarioException(Long idEvento) {
        super("El evento " + idEvento + " no es gratuito. Se requiere proceso de pago.");
    }

    public EventoNoBecarioException(String message) {
        super(message);
    }
}
