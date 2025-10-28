package com.sulaimaan.ReminderApp.dto.outgoing;

import com.sulaimaan.ReminderApp.helper.RecurrenceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTaskResponse {
    private Long id;
    private String taskTxt;
    private RecurrenceType recurrenceType;  // DAILY, WEEKLY, MONTHLY, etc.
    private ZonedDateTime nextReminderAt;   // When next reminder will fire

}
