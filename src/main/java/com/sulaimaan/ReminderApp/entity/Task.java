package com.sulaimaan.ReminderApp.entity;

import com.sulaimaan.ReminderApp.helper.RecurrenceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Entity representing a task with recurrence configuration and scheduled reminders
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Task text cannot be blank")
    @Column(nullable = false)
    private String taskTxt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    @NotNull(message = "Device token must be associated")
    private DeviceToken deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Recurrence type cannot be null")
    private RecurrenceType recurrenceType;

    @Column(nullable = false)
    @NotBlank(message = "Cron expression cannot be blank")
    private String cronExpression;

    @Column(nullable = false)
    @NotNull(message = "Creation timestamp cannot be null")
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    @NotNull(message = "Next reminder timestamp cannot be null")
    private ZonedDateTime nextReminderAt;

    @Column(nullable = true)
    private String clientTimezone;

    @OneToMany(mappedBy = "task", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Reminder> reminders;

    /**
     * Creates a new Task with specified details and scheduling information
     */
    public Task(String taskTxt, DeviceToken deviceToken, RecurrenceType recurrenceType, String cronExpression, ZonedDateTime createdAt, ZonedDateTime nextReminderAt, String clientTimezone) {
        this.taskTxt = taskTxt;
        this.deviceToken = deviceToken;
        this.recurrenceType = recurrenceType;
        this.cronExpression = cronExpression;
        this.createdAt = createdAt;
        this.nextReminderAt = nextReminderAt;
        this.clientTimezone = clientTimezone;
    }
}
