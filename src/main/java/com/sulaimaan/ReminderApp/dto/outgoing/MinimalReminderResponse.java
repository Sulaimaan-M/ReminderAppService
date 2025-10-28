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
public class MinimalReminderResponse {  // ← RENAMED
    private Long id;
    private ZonedDateTime remindedAt;  // When the reminder was actually triggered
    private Boolean isCompleted;       // Completion status
}
