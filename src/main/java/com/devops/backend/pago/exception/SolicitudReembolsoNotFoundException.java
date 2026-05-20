package com.devops.backend.pago.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SolicitudReembolsoNotFoundException extends RuntimeException {
    public SolicitudReembolsoNotFoundException(String message) {
        super(message);
    }
}
