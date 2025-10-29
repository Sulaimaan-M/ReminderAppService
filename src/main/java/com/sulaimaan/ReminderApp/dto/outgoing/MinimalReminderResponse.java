package com.sulaimaan.ReminderApp.dto.outgoing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

/**
 * Minimal response DTO containing basic reminder information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MinimalReminderResponse {
    private Long id;
    private ZonedDateTime remindedAt;
    private Boolean isCompleted;
}
