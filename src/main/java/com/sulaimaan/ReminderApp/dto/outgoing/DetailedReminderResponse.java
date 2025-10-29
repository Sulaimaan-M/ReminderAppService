package com.sulaimaan.ReminderApp.dto.outgoing;

import com.sulaimaan.ReminderApp.helper.RecurrenceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

/**
 * Response DTO containing detailed reminder information along with associated task details
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetailedReminderResponse {

    private Long reminderId;
    private ZonedDateTime remindedAt;
    private Boolean isCompleted;

    private Long taskId;
    private String taskTxt;
    private RecurrenceType recurrenceType;
}
