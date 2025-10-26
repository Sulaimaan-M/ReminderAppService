package com.sulaimaan.ReminderApp.entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "reminded_at",  nullable = false)
    private ZonedDateTime remindedAt;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    public Long getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public ZonedDateTime getRemindedAt() {
        return remindedAt;
    }

    public void setRemindedAt(ZonedDateTime remindedAt) {
        this.remindedAt = remindedAt;
    }

    public Boolean getCompleted() {
        return isCompleted;
    }

    public void setCompleted(Boolean completed) {
        isCompleted = completed;
    }
}
