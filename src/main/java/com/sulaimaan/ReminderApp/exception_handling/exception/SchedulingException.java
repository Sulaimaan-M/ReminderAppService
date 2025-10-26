package com.sulaimaan.ReminderApp.exception_handling.exception;

public class SchedulingException extends RuntimeException {

    public SchedulingException() {
        super("Scheduling operation failed");
    }

    public SchedulingException(String message) {
        super(message);
    }

    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }
}
