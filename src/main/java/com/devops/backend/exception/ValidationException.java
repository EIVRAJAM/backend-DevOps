package com.devops.backend.exception;

import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<ApiValidationError> errors;

    public ValidationException(String message, List<ApiValidationError> errors) {
        super(message);
        this.errors = errors;
    }

    public List<ApiValidationError> getErrors() {
        return errors;
    }
}
