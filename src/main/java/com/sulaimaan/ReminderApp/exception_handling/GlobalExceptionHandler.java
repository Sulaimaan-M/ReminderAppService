package com.sulaimaan.ReminderApp.exception_handling;

import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import com.sulaimaan.ReminderApp.exception_handling.dto.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidInputException(
            InvalidInputException ex, HttpServletRequest request) {

        ExceptionResponse response = new ExceptionResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                request.getRequestURI()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ExceptionResponse response = new ExceptionResponse(
                "Validation failed: " + errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                request.getRequestURI()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        ExceptionResponse response = new ExceptionResponse(
                "An unexpected error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                request.getRequestURI()
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
