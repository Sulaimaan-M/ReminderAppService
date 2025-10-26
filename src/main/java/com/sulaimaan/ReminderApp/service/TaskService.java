package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.dto.incoming.CreateTaskRequest;
import com.sulaimaan.ReminderApp.dto.incoming.UpdateTaskRequest;
import com.sulaimaan.ReminderApp.entity.DeviceToken;
import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import com.sulaimaan.ReminderApp.helper.CronStringMapper;
import com.sulaimaan.ReminderApp.helper.NextReminderCalculator;
import com.sulaimaan.ReminderApp.repository.DeviceTokenRepository;
import com.sulaimaan.ReminderApp.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    public TaskService(TaskRepository taskRepository, DeviceTokenRepository deviceTokenRepository) {
        this.taskRepository = taskRepository;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    public Task createTask(CreateTaskRequest request) {
        CronStringMapper cronMapper = new CronStringMapper();
        NextReminderCalculator reminderCalculator = new NextReminderCalculator();

        // Fetch device token
        DeviceToken deviceToken = deviceTokenRepository.findById(request.deviceId)
                .orElseThrow(() -> new InvalidInputException("Device not found with id: " + request.deviceId));

        // Build cron expression
        String cronExpression = cronMapper.buildCronExpression(
                request.timeDetail,
                request.recurrencePattern,
                request.recurrenceType
        );

        // Calculate next reminder time (converts client timezone to UTC)
        ZonedDateTime nextReminder = reminderCalculator.calculateNextReminder(
                request.timeDetail,
                request.recurrencePattern,
                request.recurrenceType
        );

        // Create and save Task entity (all times in UTC)
        Task task = new Task(
                request.taskText,
                deviceToken,
                request.recurrenceType,
                cronExpression,
                ZonedDateTime.now(ZoneOffset.UTC),
                nextReminder
        );

        return taskRepository.save(task);
    }

    public Task updateTask(Long taskId, UpdateTaskRequest request) {
        CronStringMapper cronMapper = new CronStringMapper();
        NextReminderCalculator reminderCalculator = new NextReminderCalculator();

        // Find existing task
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new InvalidInputException("Task not found with id: " + taskId));

        // Build new cron expression
        String cronExpression = cronMapper.buildCronExpression(
                request.timeDetail,
                request.recurrencePattern,
                request.recurrenceType
        );

        // Calculate new next reminder time (converts client timezone to UTC)
        ZonedDateTime nextReminder = reminderCalculator.calculateNextReminder(
                request.timeDetail,
                request.recurrencePattern,
                request.recurrenceType
        );

        // Update task fields
        existingTask.setTaskTxt(request.taskText);
        existingTask.setRecurrenceType(request.recurrenceType);
        existingTask.setCronExpression(cronExpression);
        existingTask.setNextReminderAt(nextReminder);

        // Save and return updated task
        return taskRepository.save(existingTask);
    }
}
