package com.devops.backend.evento.exception;

import com.devops.backend.exception.ResourceNotFoundException;

/**
 * Excepción lanzada cuando un evento no es encontrado
 */
public class EventoNoEncontradoException extends ResourceNotFoundException {

    public EventoNoEncontradoException(Long idEvento) {
        super("Evento con ID " + idEvento + " no encontrado");
    }

    public EventoNoEncontradoException(String message) {
        super(message);
    }
}
