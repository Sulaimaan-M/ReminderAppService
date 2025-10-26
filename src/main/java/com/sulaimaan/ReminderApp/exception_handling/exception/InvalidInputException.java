package com.sulaimaan.ReminderApp.exception_handling.exception;

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
