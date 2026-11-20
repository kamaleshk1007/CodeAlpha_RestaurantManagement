package com.codealpha.restaurantmanagement.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Problem> handleNotFound(ResourceNotFoundException ex, jakarta.servlet.http.HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({ DuplicateResourceException.class, ReservationConflictException.class, OrderProcessingException.class })
    public ResponseEntity<Problem> handleConflict(RuntimeException ex, jakarta.servlet.http.HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Problem> handleIllegalArgument(IllegalArgumentException ex, jakarta.servlet.http.HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleValidation(MethodArgumentNotValidException ex,
                                                    jakarta.servlet.http.HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        Problem problem = new Problem(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Validation failed",
                fieldErrors, request.getRequestURI());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Problem> handleRuntime(RuntimeException ex, jakarta.servlet.http.HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<Problem> build(HttpStatus status, String message,
                                          jakarta.servlet.http.HttpServletRequest request) {
        Problem problem = new Problem(Instant.now(), status.value(), message, null, request.getRequestURI());
        return ResponseEntity.status(status).body(problem);
    }

    public record Problem(Instant timestamp, int status, String error, Object details, String path) {
    }
}
