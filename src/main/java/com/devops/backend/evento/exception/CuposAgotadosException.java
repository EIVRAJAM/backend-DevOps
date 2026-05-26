package com.devops.backend.evento.exception;

/**
 * Se lanza cuando no hay cupos disponibles para inscribirse al evento.
 */
public class CuposAgotadosException extends RuntimeException {

    public CuposAgotadosException(Long eventoId) {
        super("El evento " + eventoId + " no tiene cupos disponibles.");
    }
}
