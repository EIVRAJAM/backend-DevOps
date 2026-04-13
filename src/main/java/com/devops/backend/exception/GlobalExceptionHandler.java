package com.devops.backend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ApiValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ApiValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        ApiError apiError = buildError(HttpStatus.BAD_REQUEST, "Invalid request data", request, errors);
        return new ResponseEntity<>(apiError, headers, HttpStatus.BAD_REQUEST);
    }

    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatusCode status,
            WebRequest request) {
        List<ApiValidationError> errors = ex.getFieldErrors().stream()
                .map(fieldError -> new ApiValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        ApiError apiError = buildError(HttpStatus.BAD_REQUEST, "Invalid request data", request, errors);
        return new ResponseEntity<>(apiError, headers, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.BAD_REQUEST, "Malformed JSON request", request,
                ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(apiError, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException ex, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request, ex.getErrors());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.CONFLICT, ex.getMessage(), request, ex.getErrors());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.CONFLICT, "Database constraint violation", request,
                ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntime(RuntimeException ex, WebRequest request) {
        ApiError apiError = buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request,
                ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ApiError buildError(HttpStatus status, String message, WebRequest request,
            List<ApiValidationError> validationErrors) {
        ApiError apiError = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message,
                request.getDescription(false).replace("uri=", ""));
        apiError.setErrors(validationErrors);
        return apiError;
    }

    private ApiError buildError(HttpStatus status, String message, WebRequest request, Object details) {
        ApiError apiError = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message,
                request.getDescription(false).replace("uri=", ""));
        apiError.setDetails(details);
        return apiError;
    }
}
