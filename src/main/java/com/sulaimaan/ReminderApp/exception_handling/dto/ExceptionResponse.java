package com.sulaimaan.ReminderApp.exception_handling.dto;

import java.time.ZonedDateTime;

/**
 * DTO for standardized exception response structure across the application
 */
public class ExceptionResponse {

    public String message;
    public int statusCode;
    public String status;
    public ZonedDateTime timestamp;
    public String path;

    public ExceptionResponse() {}

    public ExceptionResponse(String message, int statusCode, String status, String path) {
        this.message = message;
        this.statusCode = statusCode;
        this.status = status;
        this.timestamp = ZonedDateTime.now();
        this.path = path;
    }
}
