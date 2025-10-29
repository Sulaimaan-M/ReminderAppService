package com.sulaimaan.ReminderApp.dto.outgoing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

/**
 * Response DTO for simple one-time tasks with associated reminder information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleTaskResponse {
    private Long id;
    private String taskTxt;
    private ZonedDateTime nextReminderAt;
    private MinimalReminderResponse reminder;
}
