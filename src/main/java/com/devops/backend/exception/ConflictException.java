package com.devops.backend.exception;

import java.util.List;

public class ConflictException extends RuntimeException {

    private List<ApiValidationError> errors;

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, List<ApiValidationError> errors) {
        super(message);
        this.errors = errors;
    }

    public List<ApiValidationError> getErrors() {
        return errors;
    }
}
