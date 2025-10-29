package com.sulaimaan.ReminderApp.projection;

import java.time.ZonedDateTime;

/**
 * Projection interface for retrieving simple task data with optional associated reminder information
 */
public interface SimpleTaskProjection {
    Long getTaskId();
    String getTaskTxt();
    ZonedDateTime getNextReminderAt();

    Long getReminderId();
    ZonedDateTime getRemindedAt();
    Boolean getIsCompleted();
}
