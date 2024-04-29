package com.clearsolution.testassigment.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorModel> handleUserNotFoundException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorModel(ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler({ValidationException.class, WrongRequestException.class})
    public ResponseEntity<ErrorModel> handleValidationException(RuntimeException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorModel(ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity
                .badRequest()
                .body(errors);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(TransactionSystemException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("error message", ex.getMessage());
        Throwable cause = ex.getRootCause();
        if (cause instanceof ConstraintViolationException cve) {
            Set<ConstraintViolation<?>> violations = cve.getConstraintViolations();
            for (ConstraintViolation<?> violation : violations) {
                errors.put("cause", violation.getMessage());
            }
        }
        return ResponseEntity
                .badRequest()
                .body(errors);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Map<String, String>> handleGeneralExceptions(Exception ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("error message", ex.getMessage());
        errors.put("cause", ex.getCause().getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errors);
    }
}
