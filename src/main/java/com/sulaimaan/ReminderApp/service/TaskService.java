package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.dto.incoming.CreateTaskRequest;
import com.sulaimaan.ReminderApp.dto.incoming.UpdateTaskRequest;
import com.sulaimaan.ReminderApp.entity.DeviceToken;
import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import com.sulaimaan.ReminderApp.exception_handling.exception.SchedulingException; // Import SchedulingException
import com.sulaimaan.ReminderApp.helper.CronNextExecutionCalculator; // Import CronNextExecutionCalculator
import com.sulaimaan.ReminderApp.helper.CronStringMapper;
import com.sulaimaan.ReminderApp.helper.NextReminderCalculator;
import com.sulaimaan.ReminderApp.helper.RecurrenceType; // Import RecurrenceType
import com.sulaimaan.ReminderApp.repository.DeviceTokenRepository;
import com.sulaimaan.ReminderApp.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional; // Import Optional

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final SchedulingService schedulingService;
    private final CronNextExecutionCalculator cronNextExecCalculator; // Add Calculator

    public TaskService(TaskRepository taskRepository,
                       DeviceTokenRepository deviceTokenRepository,
                       SchedulingService schedulingService) {
        this.taskRepository = taskRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.schedulingService = schedulingService;
        this.cronNextExecCalculator = new CronNextExecutionCalculator(); // Instantiate calculator
    }

    @Transactional // Add Transactional
    public Task createTask(CreateTaskRequest request) {
        CronStringMapper cronMapper = new CronStringMapper();
        NextReminderCalculator reminderCalculator = new NextReminderCalculator();

        logger.info("TaskService.createTask | deviceId={} type={}", request.deviceId, request.recurrenceType);

        DeviceToken deviceToken = deviceTokenRepository.findById(request.deviceId)
                .orElseThrow(() -> {
                    logger.warn("TaskService.createTask | device not found id={}", request.deviceId);
                    return new InvalidInputException("Device not found with id: " + request.deviceId);
                });

        String cronExpression = cronMapper.buildCronExpression(
                request.timeDetail,
                request.recurrencePattern,
                request.recurrenceType
        );
        logger.info("TaskService.createTask | cron={}", cronExpression);

        ZonedDateTime nextReminder = reminderCalculator.calculateNextReminder(
                request.timeDetail,
                request.recurrencePattern,
                request.recurrenceType
        );
        logger.info("TaskService.createTask | nextReminderAt={}", nextReminder);

        Task task = new Task(
                request.taskText,
                deviceToken,
                request.recurrenceType,
                cronExpression,
                ZonedDateTime.now(ZoneOffset.UTC), // Store creation time in UTC
                nextReminder // Store calculated next reminder time (might be in client's zone initially)
        );

        Task savedTask = taskRepository.save(task);
        logger.info("TaskService.createTask | saved id={}", savedTask.getId());

        try { // Add try-catch for scheduling
            schedulingService.scheduleTask(savedTask);
            logger.info("TaskService.createTask | scheduled id={}", savedTask.getId());
        } catch (SchedulingException | InvalidInputException e) {
            logger.error("TaskService.createTask | scheduling failed for id={}, rolling back. Error: {}", savedTask.getId(), e.getMessage());
            // Exception will trigger rollback due to @Transactional
            throw e; // Re-throw to ensure transaction rollback and inform caller
        }

        return savedTask;
    }

    @Transactional // Add Transactional
    public Task updateTask(Long taskId, UpdateTaskRequest request) {
        CronStringMapper cronMapper = new CronStringMapper();
        NextReminderCalculator reminderCalculator = new NextReminderCalculator();

        logger.info("TaskService.updateTask | id={} type={}", taskId, request.recurrenceType);

        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.warn("TaskService.updateTask | not found id={}", taskId);
                    return new InvalidInputException("Task not found with id: " + taskId);
                });

        String cronExpression = cronMapper.buildCronExpression(
                request.timeDetail,
                request.recurrencePattern,
                request.recurrenceType
        );
        ZonedDateTime nextReminder = reminderCalculator.calculateNextReminder(
                request.timeDetail,
                request.recurrencePattern,
                request.recurrenceType
        );

        existingTask.setTaskTxt(request.taskText);
        existingTask.setRecurrenceType(request.recurrenceType);
        existingTask.setCronExpression(cronExpression);
        existingTask.setNextReminderAt(nextReminder);

        Task updatedTask = taskRepository.save(existingTask);
        logger.info("TaskService.updateTask | updated id={}", updatedTask.getId());

        try { // Add try-catch for rescheduling
            schedulingService.rescheduleTask(updatedTask);
            logger.info("TaskService.updateTask | rescheduled id={}", updatedTask.getId());
        } catch (SchedulingException | InvalidInputException e) {
            logger.error("TaskService.updateTask | rescheduling failed for id={}, rolling back. Error: {}", updatedTask.getId(), e.getMessage());
            // Exception will trigger rollback due to @Transactional
            throw e; // Re-throw to ensure transaction rollback and inform caller
        }

        return updatedTask;
    }

    @Transactional // Add Transactional
    public Task deleteTask(Long taskId) {
        logger.info("TaskService.deleteTask | id={}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.warn("TaskService.deleteTask | not found id={}", taskId);
                    return new InvalidInputException("Task not found with id: " + taskId);
                });

        try { // Add try-catch for unscheduling
            schedulingService.unscheduleTask(taskId);
            taskRepository.delete(task);
            logger.info("TaskService.deleteTask | deleted id={}", taskId);
        } catch (SchedulingException e) {
            logger.error("TaskService.deleteTask | unscheduling failed for id={}, rolling back. Error: {}", taskId, e.getMessage());
            // Exception will trigger rollback due to @Transactional
            throw e; // Re-throw to ensure transaction rollback and inform caller
        }
        return task; // Return the task that was deleted
    }

    public List<Task> getTasksByDevice(Long deviceId) {
        logger.info("TaskService.getTasksByDevice | deviceId={}", deviceId);
        // Assuming DeviceToken exists is checked elsewhere or handled by constraints
        List<Task> tasks = taskRepository.findNonSimpleTasksByDeviceId(deviceId);
        logger.info("TaskService.getTasksByDevice | deviceId={} count={}", deviceId, tasks.size());
        return tasks;
    }

    // --- New Method ---
    @Transactional
    public void updateNextReminderTime(Long taskId, ZonedDateTime currentExecutionTimeUtc) {
        logger.info("TaskService.updateNextReminderTime | taskId={}, currentExecutionTimeUtc={}", taskId, currentExecutionTimeUtc);
        Optional<Task> taskOpt = taskRepository.findById(taskId);

        if (taskOpt.isEmpty()) {
            logger.error("TaskService.updateNextReminderTime | Task not found, id={}", taskId);
            // Cannot update if task doesn't exist. Log error and return.
            return;
        }

        Task task = taskOpt.get();

        // Only update next time for recurring tasks
        if (task.getRecurrenceType() != RecurrenceType.SIMPLE) {
            try {
                // Calculate next execution time based on the *current* UTC execution time
                ZonedDateTime nextExecutionTimeUtc = cronNextExecCalculator.getNextExecutionTime(
                        task.getCronExpression(),
                        currentExecutionTimeUtc // Use the actual fire time as the base
                );
                task.setNextReminderAt(nextExecutionTimeUtc);
                taskRepository.save(task);
                logger.info("TaskService.updateNextReminderTime | Updated nextReminderAt for taskId={} to {}", taskId, nextExecutionTimeUtc);
            } catch (InvalidInputException e) {
                logger.error("TaskService.updateNextReminderTime | Failed to calculate next execution time for taskId={}. Cron: '{}', Error: {}",
                        taskId, task.getCronExpression(), e.getMessage());
                // Decide how to handle: maybe unschedule? For now, just log.
            } catch (Exception e) {
                logger.error("TaskService.updateNextReminderTime | Unexpected error calculating next time for taskId={}. Error: {}", taskId, e.getMessage(), e);
            }
        } else {
            logger.info("TaskService.updateNextReminderTime | TaskId={} is SIMPLE, not updating next time.", taskId);
            // Optionally unschedule simple tasks after they fire once
            // try {
            //     schedulingService.unscheduleTask(taskId);
            //     logger.info("TaskService.updateNextReminderTime | Unscheduled SIMPLE task id={}", taskId);
            // } catch (SchedulingException e) {
            //     logger.error("TaskService.updateNextReminderTime | Failed to unschedule SIMPLE task id={}: {}", taskId, e.getMessage());
            // }
        }
    }
    // --- End New Method ---
}
