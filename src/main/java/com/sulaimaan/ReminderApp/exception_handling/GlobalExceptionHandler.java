package com.sulaimaan.ReminderApp.exception_handling;

import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import com.sulaimaan.ReminderApp.exception_handling.exception.SchedulingException;
import com.sulaimaan.ReminderApp.exception_handling.dto.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for centralizing error handling across all REST controllers
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles InvalidInputException and returns a BAD_REQUEST response
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidInputException(
            InvalidInputException ex, HttpServletRequest request) {

        logger.warn("InvalidInputException | path={} | msg={}", request.getRequestURI(), ex.getMessage());

        ExceptionResponse response = new ExceptionResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                request.getRequestURI()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles SchedulingException and returns an INTERNAL_SERVER_ERROR response
     */
    @ExceptionHandler(SchedulingException.class)
    public ResponseEntity<ExceptionResponse> handleSchedulingException(
            SchedulingException ex, HttpServletRequest request) {

        logger.error("SchedulingException | path={} | msg={}", request.getRequestURI(), ex.getMessage(), ex);

        ExceptionResponse response = new ExceptionResponse(
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                request.getRequestURI()
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles validation errors from @Valid annotations and returns formatted error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        logger.warn("ValidationError | path={} | {}", request.getRequestURI(), errorMessage);

        ExceptionResponse response = new ExceptionResponse(
                "Validation failed: " + errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                request.getRequestURI()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Catches all unhandled exceptions and returns a generic INTERNAL_SERVER_ERROR response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        logger.error("UnhandledException | path={} | msg={}", request.getRequestURI(), ex.getMessage(), ex);

        ExceptionResponse response = new ExceptionResponse(
                "An unexpected error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                request.getRequestURI()
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
