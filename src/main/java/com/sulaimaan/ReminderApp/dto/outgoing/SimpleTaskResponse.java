package com.sulaimaan.ReminderApp.dto.outgoing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleTaskResponse {  // ← RENAMED
    private Long id;
    private String taskTxt;
    private ZonedDateTime nextReminderAt;  // When the reminder will fire

    // 🔑 Will be null until nextReminderAt time arrives
    private MinimalReminderResponse reminder;  // ← UPDATED
}
