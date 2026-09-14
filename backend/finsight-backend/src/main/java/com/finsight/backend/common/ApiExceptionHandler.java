package com.finsight.backend.common;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> badRequest(IllegalArgumentException exception) {
        return Map.of("timestamp", LocalDateTime.now(), "status", 400, "error", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validationError(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        return Map.of("timestamp", LocalDateTime.now(), "status", 400,
                "error", "Request validation failed", "fields", fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> unreadableRequest(HttpMessageNotReadableException exception) {
        return diagnosticError(400, exception);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> unexpectedError(Exception exception) {
        return diagnosticError(500, exception);
    }

    private Map<String, Object> diagnosticError(int status, Exception exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status,
                "exception", exception.getClass().getName(),
                "message", messageOrFallback(exception),
                "rootCauseMessage", messageOrFallback(rootCause));
    }

    private String messageOrFallback(Throwable throwable) {
        return throwable.getMessage() == null ? "<no message>" : throwable.getMessage();
    }
}