package com.sulaimaan.ReminderApp.dto.incoming;

import com.sulaimaan.ReminderApp.dto.incoming.minor.RecurrencePattern;
import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;
import com.sulaimaan.ReminderApp.helper.RecurrenceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating an existing task
 */
public class UpdateTaskRequest {

    @NotBlank(message = "Task text cannot be blank")
    public String taskText;

    @NotNull(message = "Recurrence type cannot be null")
    public RecurrenceType recurrenceType;

    @NotNull(message = "Time detail cannot be null")
    @Valid
    public TimeDetail timeDetail;

    @NotNull(message = "Recurrence pattern cannot be null")
    @Valid
    public RecurrencePattern recurrencePattern;
}
