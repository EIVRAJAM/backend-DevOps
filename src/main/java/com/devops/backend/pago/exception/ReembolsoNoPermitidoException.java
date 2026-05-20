package com.devops.backend.pago.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ReembolsoNoPermitidoException extends RuntimeException {
    public ReembolsoNoPermitidoException(String message) {
        super(message);
    }
}
