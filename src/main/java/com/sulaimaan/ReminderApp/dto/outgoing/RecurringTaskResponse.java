package com.sulaimaan.ReminderApp.dto.outgoing;

import com.sulaimaan.ReminderApp.helper.RecurrenceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

/**
 * Response DTO for recurring tasks with next scheduled reminder time
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTaskResponse {
    private Long id;
    private String taskTxt;
    private RecurrenceType recurrenceType;
    private ZonedDateTime nextReminderAt;
}
