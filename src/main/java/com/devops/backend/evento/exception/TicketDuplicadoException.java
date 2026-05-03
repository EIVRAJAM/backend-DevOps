package com.devops.backend.evento.exception;

/**
 * Excepción lanzada cuando un usuario intenta inscribirse
 * a un evento al que ya está inscrito (violación UNIQUE id_usuario + id_evento).
 */
public class TicketDuplicadoException extends RuntimeException {

    public TicketDuplicadoException(Long idUsuario, Long idEvento) {
        super("El usuario " + idUsuario + " ya está inscrito en el evento " + idEvento);
    }

    public TicketDuplicadoException(String message) {
        super(message);
    }
}
