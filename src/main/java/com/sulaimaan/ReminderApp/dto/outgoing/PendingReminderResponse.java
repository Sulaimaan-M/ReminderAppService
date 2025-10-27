package com.sulaimaan.ReminderApp.dto.outgoing;

import com.sulaimaan.ReminderApp.entity.Reminder;
import com.sulaimaan.ReminderApp.entity.Task;

import java.time.ZonedDateTime;

public class PendingReminderResponse {

    private Long id;                  // reminder instance id
    private Long taskId;              // parent task id
    private String taskText;          // task text
    private String recurrenceType;    // DAILY/WEEKLY/MONTHLY/YEARLY (no cron)
    private ZonedDateTime remindedAt; // when this reminder fired
    private Boolean isCompleted;      // should be false in this list, but returned anyway

    public PendingReminderResponse() {}

    public PendingReminderResponse(Long id,
                                   Long taskId,
                                   String taskText,
                                   String recurrenceType,
                                   ZonedDateTime remindedAt,
                                   Boolean isCompleted) {
        this.id = id;
        this.taskId = taskId;
        this.taskText = taskText;
        this.recurrenceType = recurrenceType;
        this.remindedAt = remindedAt;
        this.isCompleted = isCompleted;
    }

    public static PendingReminderResponse from(Reminder r) {
        Task t = r.getTask();
        return new PendingReminderResponse(
                r.getId(),
                t.getId(),
                t.getTaskTxt(),
                t.getRecurrenceType().name(),
                r.getRemindedAt(),
                r.getCompleted()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getTaskText() {
        return taskText;
    }

    public String getRecurrenceType() {
        return recurrenceType;
    }

    public ZonedDateTime getRemindedAt() {
        return remindedAt;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }
}
