package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.dto.incoming.CreateTaskRequest;
import com.sulaimaan.ReminderApp.dto.incoming.UpdateTaskRequest;
import com.sulaimaan.ReminderApp.entity.DeviceToken;
import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import com.sulaimaan.ReminderApp.exception_handling.exception.SchedulingException;
import com.sulaimaan.ReminderApp.helper.CronNextExecutionCalculator;
import com.sulaimaan.ReminderApp.helper.CronStringMapper;
import com.sulaimaan.ReminderApp.helper.NextReminderCalculator;
import com.sulaimaan.ReminderApp.helper.RecurrenceType;
import com.sulaimaan.ReminderApp.repository.DeviceTokenRepository;
import com.sulaimaan.ReminderApp.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class); // 🪵 Logger instance

    private final TaskRepository taskRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final SchedulingService schedulingService;
    private final CronNextExecutionCalculator cronNextExecCalculator;

    public TaskService(TaskRepository taskRepository,
                       DeviceTokenRepository deviceTokenRepository,
                       SchedulingService schedulingService) {
        this.taskRepository = taskRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.schedulingService = schedulingService;
        this.cronNextExecCalculator = new CronNextExecutionCalculator();
    }

    @Transactional
    public Task createTask(CreateTaskRequest request) {
        CronStringMapper cronMapper = new CronStringMapper();
        NextReminderCalculator reminderCalculator = new NextReminderCalculator();

        logger.info("➕ TaskService.createTask | deviceId={} type={} text='{}' time={}:{}:{} zone={}",
                request.deviceId, request.recurrenceType, request.taskText,
                request.timeDetail.hours, request.timeDetail.minutes, request.timeDetail.seconds, request.timeDetail.timezone);

        DeviceToken deviceToken = deviceTokenRepository.findById(request.deviceId)
                .orElseThrow(() -> {
                    logger.warn("⚠️ TaskService.createTask | device not found id={}", request.deviceId);
                    return new InvalidInputException("Device not found with id: " + request.deviceId);
                });

        String cronExpression = cronMapper.buildCronExpression(
                request.timeDetail,
                request.recurrencePattern,
                request.recurrenceType
        );
        logger.info("⏳ TaskService.createTask | Calculated Cron (UTC): {}", cronExpression);

        ZonedDateTime nextReminder = reminderCalculator.calculateNextReminder(
                request.timeDetail,
                request.recurrencePattern,
                request.recurrenceType
        );
        logger.info("➡️ TaskService.createTask | Calculated Next Reminder (Client Zone): {}", nextReminder);

        // --- Store the client's timezone string ---
        String clientTimezone = request.timeDetail.timezone;
        logger.debug(" TaskService.createTask | Storing client timezone: {}", clientTimezone);

        Task task = new Task(
                request.taskText,
                deviceToken,
                request.recurrenceType,
                cronExpression,
                ZonedDateTime.now(ZoneOffset.UTC),
                nextReminder,
                clientTimezone // Pass the timezone string to the constructor
        );

        logger.debug("💾 TaskService.createTask | Task entity before save: text='{}', type={}, cron='{}', nextAt='{}', deviceId={}, clientZone='{}'",
                task.getTaskTxt(), task.getRecurrenceType(), task.getCronExpression(), task.getNextReminderAt(), task.getDeviceToken().getId(), task.getClientTimezone()); // Log timezone

        Task savedTask = taskRepository.save(task);
        logger.info("✅ TaskService.createTask | Saved Task id={}", savedTask.getId());

        try {
            logger.info("🗓️ TaskService.createTask | Scheduling job for taskId={}", savedTask.getId());
            schedulingService.scheduleTask(savedTask);
            logger.info("👍 TaskService.createTask | Scheduled job successfully for taskId={}", savedTask.getId());
        } catch (SchedulingException | InvalidInputException e) {
            logger.error("❌ TaskService.createTask | Scheduling failed for taskId={}, rolling back. Error: {}", savedTask.getId(), e.getMessage());
            throw e;
        }

        return savedTask;
    }

    @Transactional
    public Task updateTask(Long taskId, UpdateTaskRequest request) {
        CronStringMapper cronMapper = new CronStringMapper();
        NextReminderCalculator reminderCalculator = new NextReminderCalculator();

        logger.info("✏️ TaskService.updateTask | taskId={} type={} text='{}' time={}:{}:{} zone={}",
                taskId, request.recurrenceType, request.taskText,
                request.timeDetail.hours, request.timeDetail.minutes, request.timeDetail.seconds, request.timeDetail.timezone);

        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.warn("⚠️ TaskService.updateTask | Task not found id={}", taskId);
                    return new InvalidInputException("Task not found with id: " + taskId);
                });

        String cronExpression = cronMapper.buildCronExpression(
                request.timeDetail,
                request.recurrencePattern,
                request.recurrenceType
        );
        logger.info("⏳ TaskService.updateTask | Calculated Cron (UTC): {}", cronExpression);

        ZonedDateTime nextReminder = reminderCalculator.calculateNextReminder(
                request.timeDetail,
                request.recurrencePattern,
                request.recurrenceType
        );
        logger.info("➡️ TaskService.updateTask | Calculated Next Reminder (Client Zone): {}", nextReminder);

        // --- Store the client's timezone string on update ---
        String clientTimezone = request.timeDetail.timezone;
        logger.debug(" TaskService.updateTask | Updating client timezone to: {}", clientTimezone);


        existingTask.setTaskTxt(request.taskText);
        existingTask.setRecurrenceType(request.recurrenceType);
        existingTask.setCronExpression(cronExpression);
        existingTask.setNextReminderAt(nextReminder);
        existingTask.setClientTimezone(clientTimezone); // Set the timezone

        logger.debug("💾 TaskService.updateTask | Task entity before update: id={}, text='{}', type={}, cron='{}', nextAt='{}', clientZone='{}'",
                existingTask.getId(), existingTask.getTaskTxt(), existingTask.getRecurrenceType(), existingTask.getCronExpression(), existingTask.getNextReminderAt(), existingTask.getClientTimezone()); // Log timezone

        Task updatedTask = taskRepository.save(existingTask);
        logger.info("✅ TaskService.updateTask | Updated Task id={}", updatedTask.getId());

        try {
            logger.info("🔄 TaskService.updateTask | Rescheduling job for taskId={}", updatedTask.getId());
            schedulingService.rescheduleTask(updatedTask);
            logger.info("👍 TaskService.updateTask | Rescheduled job successfully for taskId={}", updatedTask.getId());
        } catch (SchedulingException | InvalidInputException e) {
            logger.error("❌ TaskService.updateTask | Rescheduling failed for taskId={}, rolling back. Error: {}", updatedTask.getId(), e.getMessage());
            throw e;
        }

        return updatedTask;
    }

    @Transactional
    public Task deleteTask(Long taskId) {
        logger.info("🗑️ TaskService.deleteTask | taskId={}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.warn("⚠️ TaskService.deleteTask | Task not found id={}", taskId);
                    return new InvalidInputException("Task not found with id: " + taskId);
                });

        try {
            logger.info("🚫 TaskService.deleteTask | Unscheduling job for taskId={}", taskId);
            schedulingService.unscheduleTask(taskId);
            taskRepository.delete(task);
            logger.info("✅ TaskService.deleteTask | Unscheduled and Deleted Task id={}", taskId);
        } catch (SchedulingException e) {
            logger.error("❌ TaskService.deleteTask | Unscheduling failed for taskId={}, rolling back. Error: {}", taskId, e.getMessage());
            throw e;
        }
        return task;
    }

    public List<Task> getTasksByDevice(Long deviceId) {
        logger.info("📋 TaskService.getTasksByDevice | deviceId={}", deviceId);
        List<Task> tasks = taskRepository.findNonSimpleTasksByDeviceId(deviceId);

        if (logger.isDebugEnabled()) {
            tasks.forEach(task -> logger.debug("  📋 Retrieved Task | id={}, nextReminderAt='{}', clientTimezone='{}' (Raw from DB/JPA)", task.getId(), task.getNextReminderAt(), task.getClientTimezone())); // Log timezone
        }

        logger.info("✅ TaskService.getTasksByDevice | deviceId={} count={}", deviceId, tasks.size());
        return tasks;
    }

    @Transactional
    public void updateNextReminderTime(Long taskId, ZonedDateTime currentExecutionTimeUtc) {
        logger.info("⏭️ TaskService.updateNextReminderTime | taskId={}, currentExecutionTimeUtc={}", taskId, currentExecutionTimeUtc);
        Optional<Task> taskOpt = taskRepository.findById(taskId);

        if (taskOpt.isEmpty()) {
            logger.error("❌ TaskService.updateNextReminderTime | Task not found, id={}", taskId);
            return;
        }

        Task task = taskOpt.get();

        if (task.getRecurrenceType() != RecurrenceType.SIMPLE) {
            try {
                logger.debug("  ⏭️ Calculating next UTC time | cron='{}', baseTime='{}'", task.getCronExpression(), currentExecutionTimeUtc);

                ZonedDateTime nextExecutionTimeUtc = cronNextExecCalculator.getNextExecutionTime(
                        task.getCronExpression(),
                        currentExecutionTimeUtc
                );
                logger.info("  ➡️ New nextExecutionTimeUtc: {}", nextExecutionTimeUtc);

                // --- IMPORTANT: Store the UTC time calculated from cron ---
                // We keep the clientTimezone field as is, but update nextReminderAt to the *next* UTC instant.
                // The conversion back to client zone happens when sending the response.
                task.setNextReminderAt(nextExecutionTimeUtc);
                logger.debug("  💾 Saving updated nextReminderAt='{}' (UTC) for taskId={}", nextExecutionTimeUtc, taskId);
                taskRepository.save(task);
                logger.info("✅ TaskService.updateNextReminderTime | Updated nextReminderAt successfully for taskId={}", taskId);
            } catch (InvalidInputException e) {
                logger.error("❌ TaskService.updateNextReminderTime | Failed to calculate next execution time for taskId={}. Cron: '{}', Error: {}",
                        taskId, task.getCronExpression(), e.getMessage());
            } catch (Exception e) {
                logger.error("❌ TaskService.updateNextReminderTime | Unexpected error calculating next time for taskId={}. Error: {}", taskId, e.getMessage(), e);
            }
        } else {
            logger.info("ℹ️ TaskService.updateNextReminderTime | TaskId={} is SIMPLE, not updating next time.", taskId);
        }
    }
}
