package com.devops.backend.evento.exception;

import com.devops.backend.exception.ConflictException;

/**
 * Excepción lanzada cuando hay un conflicto de validación en operaciones del
 * evento
 * Por ejemplo, transiciones de estado inválidas, estados inconsistentes, etc.
 */
public class ValidacionEventoException extends ConflictException {

    public ValidacionEventoException(String message) {
        super(message);
    }

    public ValidacionEventoException(String message,
            java.util.List<com.devops.backend.exception.ApiValidationError> errors) {
        super(message, errors);
    }
}
