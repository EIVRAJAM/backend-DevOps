package com.devops.backend.evento.exception;

/**
 * Excepción lanzada cuando un usuario no tiene permisos para editar un evento
 * o cuando el evento está en un estado que no permite edición
 */
public class EventoNoEditableException extends RuntimeException {

    public EventoNoEditableException(String message) {
        super(message);
    }

    public EventoNoEditableException(String message, Throwable cause) {
        super(message, cause);
    }
}
