package com.sulaimaan.ReminderApp.dto.outgoing;

import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.helper.RecurrenceType;

import java.time.ZonedDateTime;

public class TaskResponse {

    public Long id;
    public String taskText;
    public ZonedDateTime createdAt;
    public ZonedDateTime nextReminderAt;
    public String cronExpression;
    public RecurrenceType recurrenceType;
    public Long deviceId;

    public TaskResponse() {}

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.taskText = task.getTaskTxt();
        this.createdAt = task.getCreatedAt();
        this.nextReminderAt = task.getNextReminderAt();
        this.cronExpression = task.getCronExpression();
        this.recurrenceType = task.getRecurrenceType();
        this.deviceId = task.getDeviceToken().getId();
    }

    public static TaskResponse from(Task task) {
        return new TaskResponse(task);
    }
}
