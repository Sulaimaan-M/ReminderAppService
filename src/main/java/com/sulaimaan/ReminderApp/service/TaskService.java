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
                ZonedDateTime.now(ZoneOffset.UTC),
                nextReminder
        );

        Task savedTask = taskRepository.save(task);
        logger.info("TaskService.createTask | saved id={}", savedTask.getId());

        schedulingService.scheduleTask(savedTask);
        logger.info("TaskService.createTask | scheduled id={}", savedTask.getId());

        return savedTask;
    }

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

        schedulingService.rescheduleTask(updatedTask);
        logger.info("TaskService.updateTask | rescheduled id={}", updatedTask.getId());

        return updatedTask;
    }

    public Task deleteTask(Long taskId) {
        logger.info("TaskService.deleteTask | id={}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.warn("TaskService.deleteTask | not found id={}", taskId);
                    return new InvalidInputException("Task not found with id: " + taskId);
                });

        schedulingService.unscheduleTask(taskId);
        taskRepository.delete(task);
        logger.info("TaskService.deleteTask | deleted id={}", taskId);
        return task;
    }

    public List<Task> getTasksByDevice(Long deviceId) {
        logger.info("TaskService.getTasksByDevice | deviceId={}", deviceId);
        List<Task> tasks = taskRepository.findNonSimpleTasksByDeviceId(deviceId);
        logger.info("TaskService.getTasksByDevice | deviceId={} count={}", deviceId, tasks.size());
        return tasks;
    }
}
