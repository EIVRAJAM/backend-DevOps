package com.devops.backend.evento.exception;

/**
 * Se lanza cuando un usuario intenta inscribirse a un evento
 * que no está en estado PUBLICADO + ACTIVO.
 */
public class EventoNoInscribibleException extends RuntimeException {

    public EventoNoInscribibleException(Long eventoId, String estadoEvento, String estado) {
        super("No es posible inscribirse al evento " + eventoId
                + ". Estado requerido: PUBLICADO + ACTIVO. Estado actual: "
                + estadoEvento + " / " + estado);
    }
}
