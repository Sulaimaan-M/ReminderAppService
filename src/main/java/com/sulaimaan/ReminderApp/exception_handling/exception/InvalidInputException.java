package com.sulaimaan.ReminderApp.exception_handling.exception;

/**
 * Custom exception thrown when invalid input is provided to the application
 */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException() {
        super("Invalid input provided");
    }

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
