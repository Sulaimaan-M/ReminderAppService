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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final SchedulingService schedulingService;

    public TaskService(TaskRepository taskRepository,
                       DeviceTokenRepository deviceTokenRepository,
                       SchedulingService schedulingService) {
        this.taskRepository = taskRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.schedulingService = schedulingService;
    }

    public Task createTask(CreateTaskRequest request) {
        logger.info("🟢 TaskService.createTask | 1/6: Start | deviceId={}", request.deviceId);
        DeviceToken deviceToken = deviceTokenRepository.findById(request.deviceId)
                .orElseThrow(() -> {
                    logger.warn("🔴 TaskService.createTask | 2/6: Fail - Device not found | id={}", request.deviceId);
                    return new InvalidInputException("Device not found with id: " + request.deviceId);
                });
        logger.info("🟢 TaskService.createTask | 2/6: Found device token | id={}", deviceToken.getId());

        String cronExpression = new CronStringMapper().buildCronExpression(
                request.timeDetail, request.recurrencePattern, request.recurrenceType
        );
        logger.info("🟢 TaskService.createTask | 3/6: Built cron expression | cron='{}'", cronExpression);

        ZonedDateTime nextReminder = new NextReminderCalculator().calculateNextReminder(
                request.timeDetail, request.recurrencePattern, request.recurrenceType
        );
        logger.info("🟢 TaskService.createTask | 4/6: Calculated next reminder time | nextReminderAt={}", nextReminder);

        Task task = new Task(request.taskText, deviceToken, request.recurrenceType, cronExpression,
                ZonedDateTime.now(ZoneOffset.UTC), nextReminder);
        Task savedTask = taskRepository.save(task);
        logger.info("🟢 TaskService.createTask | 5/6: Saved task to DB | id={}", savedTask.getId());

        schedulingService.scheduleTask(savedTask);
        logger.info("🟢 TaskService.createTask | 6/6: Done - Scheduled task | id={}", savedTask.getId());
        return savedTask;
    }

    public Task updateTask(Long taskId, UpdateTaskRequest request) {
        logger.info("🔵 TaskService.updateTask | 1/5: Start | taskId={}", taskId);
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.warn("🔴 TaskService.updateTask | 2/5: Fail - Task not found | id={}", taskId);
                    return new InvalidInputException("Task not found with id: " + taskId);
                });
        logger.info("🔵 TaskService.updateTask | 2/5: Found task");

        String cronExpression = new CronStringMapper().buildCronExpression(
                request.timeDetail, request.recurrencePattern, request.recurrenceType
        );
        ZonedDateTime nextReminder = new NextReminderCalculator().calculateNextReminder(
                request.timeDetail, request.recurrencePattern, request.recurrenceType
        );

        existingTask.setTaskTxt(request.taskText);
        existingTask.setRecurrenceType(request.recurrenceType);
        existingTask.setCronExpression(cronExpression);
        existingTask.setNextReminderAt(nextReminder);

        Task updatedTask = taskRepository.save(existingTask);
        logger.info("🔵 TaskService.updateTask | 3/5: Updated task in DB | id={}", updatedTask.getId());

        schedulingService.rescheduleTask(updatedTask);
        logger.info("🔵 TaskService.updateTask | 4/5: Rescheduled task");

        logger.info("🔵 TaskService.updateTask | 5/5: Done");
        return updatedTask;
    }

    public void deleteTask(Long taskId) {
        logger.info("🟡 TaskService.deleteTask | 1/3: Start | taskId={}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.warn("🔴 TaskService.deleteTask | 2/3: Fail - Task not found | id={}", taskId);
                    return new InvalidInputException("Task not found with id: " + taskId);
                });

        schedulingService.unscheduleTask(taskId);
        logger.info("🟡 TaskService.deleteTask | 2/3: Unscheduled task");

        taskRepository.delete(task);
        logger.info("🟡 TaskService.deleteTask | 3/3: Done - Deleted task from DB");
    }

    public List<Task> getTasksByDevice(Long deviceId) {
        logger.info("🔵 TaskService.getTasksByDevice | deviceId={}", deviceId);
        List<Task> tasks = taskRepository.findNonSimpleTasksByDeviceId(deviceId);
        logger.info("🔵 TaskService.getTasksByDevice | deviceId={} found {} tasks", deviceId, tasks.size());
        return tasks;
    }
}
