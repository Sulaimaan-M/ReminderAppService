package com.sulaimaan.ReminderApp.projection;

import java.time.ZonedDateTime;

public interface SimpleTaskProjection {
    Long getTaskId();
    String getTaskTxt();
    ZonedDateTime getNextReminderAt();

    // Reminder fields (null if no reminder exists)
    Long getReminderId();
    ZonedDateTime getRemindedAt();
    Boolean getIsCompleted();
}
