package com.devops.backend.evento.exception;

/**
 * Excepcion lanzada cuando un usuario intenta inscribirse
 * a un evento donde ya tiene un ticket activo.
 */
public class TicketDuplicadoException extends RuntimeException {

    public TicketDuplicadoException(Long idUsuario, Long idEvento) {
        super("El usuario " + idUsuario + " ya tiene una inscripcion activa en el evento " + idEvento);
    }

    public TicketDuplicadoException(String message) {
        super(message);
    }
}
