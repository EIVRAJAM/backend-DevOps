package com.devops.backend.evento.exception;

public class TicketNoValidoParaCheckinException extends RuntimeException {
    public TicketNoValidoParaCheckinException(String message) {
        super(message);
    }
}
