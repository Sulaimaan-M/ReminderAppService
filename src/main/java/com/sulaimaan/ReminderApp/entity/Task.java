package com.sulaimaan.ReminderApp.entity;

import com.sulaimaan.ReminderApp.helper.IntervalType;
import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "task_txt", nullable = false)
    private String taskTxt;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "next_reminder_at", nullable = false)
    private ZonedDateTime nextReminderAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private DeviceToken deviceToken;

    @Column(name = "cron_exp", nullable = false)
    private String cronExpression;

    @Column(name = "interval_type", nullable = false)
    private IntervalType intervalType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskTxt() {
        return taskTxt;
    }

    public void setTaskTxt(String taskTxt) {
        this.taskTxt = taskTxt;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public DeviceToken getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(DeviceToken deviceToken) {
        this.deviceToken = deviceToken;
    }

    public ZonedDateTime getNextReminderAt() {
        return nextReminderAt;
    }

    public void setNextReminderAt(ZonedDateTime nextReminderAt) {
        this.nextReminderAt = nextReminderAt;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public IntervalType getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(IntervalType intervalType) {
        this.intervalType = intervalType;
    }
}
