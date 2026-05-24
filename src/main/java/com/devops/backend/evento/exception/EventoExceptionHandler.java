package com.devops.backend.evento.exception;

import com.devops.backend.exception.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

/**
 * Manejador centralizado de excepciones para el módulo de eventos
 * Específicamente para excepciones de negocio del evento (validación, permisos,
 * estado)
 */
@RestControllerAdvice(basePackages = "com.devops.backend.evento")
public class EventoExceptionHandler {

    /**
     * Maneja eventos no encontrados (404)
     */
    @ExceptionHandler(EventoNoEncontradoException.class)
    public ResponseEntity<ApiError> handleEventoNoEncontrado(EventoNoEncontradoException ex, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja falta de permisos para editar evento (403 Forbidden)
     */
    @ExceptionHandler(EventoNoEditableException.class)
    public ResponseEntity<ApiError> handleEventoNoEditable(EventoNoEditableException ex, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request);
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    /**
     * Maneja validaciones de evento fallidas (409 Conflict)
     * Ejemplo: transición de estado inválida
     */
    @ExceptionHandler(ValidacionEventoException.class)
    public ResponseEntity<ApiError> handleValidacionEvento(ValidacionEventoException ex, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
        if (ex.getErrors() != null) {
            apiError.setErrors(ex.getErrors());
        }
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Maneja intento de doble inscripción al mismo evento (409 Conflict)
     */
    @ExceptionHandler(TicketDuplicadoException.class)
    public ResponseEntity<ApiError> handleTicketDuplicado(TicketDuplicadoException ex, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Maneja inscripción gratuita sobre evento de pago (400 Bad Request)
     */
    @ExceptionHandler(EventoNoGratisException.class)
    public ResponseEntity<ApiError> handleEventoNoGratis(EventoNoGratisException ex, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja intento de inscripción a evento no disponible (409 Conflict)
     * Ejemplo: evento en BORRADOR, CERRADO o CANCELADO
     */
    @ExceptionHandler(EventoNoInscribibleException.class)
    public ResponseEntity<ApiError> handleEventoNoInscribible(EventoNoInscribibleException ex, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Maneja falta de cupos disponibles en el evento (409 Conflict)
     */
    @ExceptionHandler(CuposAgotadosException.class)
    public ResponseEntity<ApiError> handleCuposAgotados(CuposAgotadosException ex, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Maneja intento de check-in duplicado (409 Conflict)
     */
    @ExceptionHandler(CheckinYaRealizadoException.class)
    public ResponseEntity<ApiError> handleCheckinYaRealizado(CheckinYaRealizadoException ex, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Maneja ticket no valido para check-in (409 Conflict)
     */
    @ExceptionHandler(TicketNoValidoParaCheckinException.class)
    public ResponseEntity<ApiError> handleTicketNoValidoParaCheckin(TicketNoValidoParaCheckinException ex, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Construye un objeto ApiError estándar
     */
    private ApiError buildError(HttpStatus status, String message, WebRequest request) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getDescription(false).replace("uri=", ""));
    }
}
