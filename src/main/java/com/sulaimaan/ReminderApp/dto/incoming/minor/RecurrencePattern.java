package com.sulaimaan.ReminderApp.dto.incoming.minor;

import jakarta.validation.constraints.NotBlank;

public class RecurrencePattern {

    @NotBlank(message = "Day of month cannot be blank")
    public String dayOfMonth;

    @NotBlank(message = "Day of week cannot be blank")
    public String dayOfWeek;

    @NotBlank(message = "Month cannot be blank")
    public String month;

    @NotBlank(message = "Year cannot be blank")
    public String year;
}
