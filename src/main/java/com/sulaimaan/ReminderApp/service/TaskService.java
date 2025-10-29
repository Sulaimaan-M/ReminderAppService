package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.dto.incoming.CreateTaskRequest;
import com.sulaimaan.ReminderApp.dto.incoming.UpdateTaskRequest;
import com.sulaimaan.ReminderApp.dto.incoming.minor.RecurrencePattern;
import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;
import com.sulaimaan.ReminderApp.dto.outgoing.RecurringTaskResponse;
import com.sulaimaan.ReminderApp.dto.outgoing.SimpleTaskResponse;
import com.sulaimaan.ReminderApp.dto.outgoing.MinimalReminderResponse;
import com.sulaimaan.ReminderApp.entity.DeviceToken;
import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import com.sulaimaan.ReminderApp.exception_handling.exception.SchedulingException;
import com.sulaimaan.ReminderApp.helper.CronStringMapper;
import com.sulaimaan.ReminderApp.helper.NextReminderCalculator;
import com.sulaimaan.ReminderApp.helper.RecurrenceType;
import com.sulaimaan.ReminderApp.helper.TimeDetailToZonedDateTimeConverter;
import com.sulaimaan.ReminderApp.projection.SimpleTaskProjection;
import com.sulaimaan.ReminderApp.repository.DeviceTokenRepository;
import com.sulaimaan.ReminderApp.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing task creation, updates, deletion, and scheduling
 */
@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final SchedulingService schedulingService;
    private final NextReminderCalculator nextReminderCalculator;
    private final CronStringMapper cronStringMapper;

    public TaskService(TaskRepository taskRepository,
                       DeviceTokenRepository deviceTokenRepository,
                       SchedulingService schedulingService) {
        this.taskRepository = taskRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.schedulingService = schedulingService;
        this.nextReminderCalculator = new NextReminderCalculator();
        this.cronStringMapper = new CronStringMapper();
    }

    /**
     * Creates a new task with calculated cron expression and next reminder time, then schedules it
     */
    @Transactional
    public Task createTask(CreateTaskRequest request) {
        logger.info("CREATE_TASK | deviceId={}, type={}, text='{}', time={}:{}:{}, zone={}",
                request.deviceId, request.recurrenceType, request.taskText,
                request.timeDetail.hours, request.timeDetail.minutes, request.timeDetail.seconds, request.timeDetail.timezone);

        DeviceToken deviceToken = deviceTokenRepository.findById(request.deviceId)
                .orElseThrow(() -> {
                    logger.warn("CREATE_TASK | Device not found: deviceId={}", request.deviceId);
                    return new InvalidInputException("Device not found with id: " + request.deviceId);
                });

        SchedulingData schedulingData = calculateSchedulingData(
                request.timeDetail,
                request.recurrencePattern,
                request.recurrenceType
        );

        Task task = new Task(
                request.taskText,
                deviceToken,
                request.recurrenceType,
                schedulingData.cronExpression,
                ZonedDateTime.now(ZoneOffset.UTC),
                schedulingData.nextReminder,
                schedulingData.clientTimezone
        );

        Task savedTask = taskRepository.save(task);
        logger.info("CREATE_TASK | Task saved to database: taskId={}", savedTask.getId());

        scheduleTaskJob(savedTask);

        return savedTask;
    }

    /**
     * Updates an existing task with new details, recalculates scheduling, and reschedules the job
     */
    @Transactional
    public void updateTask(Long taskId, UpdateTaskRequest request) {
        logger.info("UPDATE_TASK | taskId={}, type={}, text='{}', time={}:{}:{}, zone={}",
                taskId, request.recurrenceType, request.taskText,
                request.timeDetail.hours, request.timeDetail.minutes, request.timeDetail.seconds, request.timeDetail.timezone);

        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.warn("UPDATE_TASK | Task not found: taskId={}", taskId);
                    return new InvalidInputException("Task not found with id: " + taskId);
                });

        SchedulingData schedulingData = calculateSchedulingData(
                request.timeDetail,
                request.recurrencePattern,
                request.recurrenceType
        );

        existingTask.setTaskTxt(request.taskText);
        existingTask.setRecurrenceType(request.recurrenceType);
        existingTask.setCronExpression(schedulingData.cronExpression);
        existingTask.setNextReminderAt(schedulingData.nextReminder);
        existingTask.setClientTimezone(schedulingData.clientTimezone);

        Task updatedTask = taskRepository.save(existingTask);
        logger.info("UPDATE_TASK | Task updated in database: taskId={}", updatedTask.getId());

        rescheduleTaskJob(updatedTask);

    }

    /**
     * Deletes a task and unschedules its associated Quartz job
     */
    @Transactional
    public void deleteTask(Long taskId) {
        logger.info("DELETE_TASK | Deleting task: taskId={}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.warn("DELETE_TASK | Task not found: taskId={}", taskId);
                    return new InvalidInputException("Task not found with id: " + taskId);
                });

        try {
            schedulingService.unscheduleTask(taskId);
            taskRepository.delete(task);
            logger.info("DELETE_TASK | Task deleted successfully: taskId={}", taskId);
        } catch (SchedulingException e) {
            logger.error("DELETE_TASK | Unscheduling failed, rolling back: taskId={}", taskId);
            throw e;
        }

    }

    /**
     * Updates the next reminder time for recurring tasks after a job execution
     */
    @Transactional
    public void updateNextReminderTime(Long taskId, ZonedDateTime currentExecutionTimeUtc) {
        logger.info("UPDATE_NEXT_TIME | taskId={}, executionTime={}", taskId, currentExecutionTimeUtc);

        Optional<Task> taskOpt = taskRepository.findById(taskId);

        if (taskOpt.isEmpty()) {
            logger.error("UPDATE_NEXT_TIME | Task not found: taskId={}", taskId);
            return;
        }

        Task task = taskOpt.get();

        if (task.getRecurrenceType() == RecurrenceType.SIMPLE) {
            logger.info("UPDATE_NEXT_TIME | Skipping SIMPLE task: taskId={}", taskId);
            return;
        }

        try {
            ZoneId clientZone = ZoneId.of(task.getClientTimezone());
            ZonedDateTime current = task.getNextReminderAt().withZoneSameInstant(clientZone);

            logger.debug("UPDATE_NEXT_TIME | Converted UTC to client zone: {} -> {}", task.getNextReminderAt(), current);

            ZonedDateTime nextExecutionTime = nextReminderCalculator.calculateNext(
                    current,
                    task.getCronExpression(),
                    task.getRecurrenceType()
            );

            task.setNextReminderAt(nextExecutionTime);
            Task updatedTask = taskRepository.save(task);

            logger.info("UPDATE_NEXT_TIME | Next reminder updated: taskId={}, nextTime={}", updatedTask.getId(), nextExecutionTime);
        } catch (InvalidInputException e) {
            logger.error("UPDATE_NEXT_TIME | Calculation failed: taskId={}, error={}", taskId, e.getMessage());
        } catch (Exception e) {
            logger.error("UPDATE_NEXT_TIME | Unexpected error: taskId={}, error={}", taskId, e.getMessage(), e);
        }
    }

    /**
     * Retrieves all recurring tasks for a specific device
     */
    public List<RecurringTaskResponse> getRecurringTasks(Long deviceId) {
        logger.info("GET_RECURRING_TASKS | deviceId={}", deviceId);
        List<RecurringTaskResponse> tasks = taskRepository.findRecurringTasksByDeviceId(deviceId);
        logger.info("GET_RECURRING_TASKS | Retrieved {} tasks for deviceId={}", tasks.size(), deviceId);
        return tasks;
    }

    /**
     * Retrieves all simple (one-time) tasks for a specific device with their reminder information
     */
    public List<SimpleTaskResponse> getSimpleTasks(Long deviceId) {
        logger.info("GET_SIMPLE_TASKS | deviceId={}", deviceId);

        List<SimpleTaskProjection> projections = taskRepository.findSimpleTaskProjectionsByDeviceId(deviceId);

        List<SimpleTaskResponse> responses = projections.stream()
                .map(p -> new SimpleTaskResponse(
                        p.getTaskId(),
                        p.getTaskTxt(),
                        p.getNextReminderAt(),
                        p.getReminderId() != null
                                ? new MinimalReminderResponse(
                                p.getReminderId(),
                                p.getRemindedAt(),
                                p.getIsCompleted()
                        )
                                : null
                ))
                .toList();

        logger.info("GET_SIMPLE_TASKS | Retrieved {} tasks for deviceId={}", responses.size(), deviceId);
        return responses;
    }

    /**
     * Calculates scheduling data (clientTime, nextReminder, cronExpression) from time details and pattern
     */
    private SchedulingData calculateSchedulingData(TimeDetail timeDetail, RecurrencePattern pattern, RecurrenceType type) {
        ZonedDateTime clientTime = TimeDetailToZonedDateTimeConverter.convert(timeDetail);
        logger.debug("CALCULATE_SCHEDULE | Converted TimeDetail to clientTime: {}", clientTime);

        ZonedDateTime nextReminder = nextReminderCalculator.calculateNext(clientTime, pattern, type);
        logger.debug("CALCULATE_SCHEDULE | Next reminder calculated: {}", nextReminder);

        String cronExpression = cronStringMapper.buildCronExpression(nextReminder, pattern, type);
        logger.debug("CALCULATE_SCHEDULE | Cron expression built: {}", cronExpression);

        return new SchedulingData(nextReminder, cronExpression, timeDetail.timezone);
    }

    /**
     * Schedules a Quartz job for the given task
     */
    private void scheduleTaskJob(Task task) {
        try {
            schedulingService.scheduleTask(task);
            logger.info("SCHEDULE_JOB | Job scheduled successfully: taskId={}", task.getId());
        } catch (SchedulingException | InvalidInputException e) {
            logger.error("SCHEDULE_JOB | Scheduling failed, rolling back: taskId={}", task.getId());
            throw e;
        }
    }

    /**
     * Reschedules a Quartz job for the given task
     */
    private void rescheduleTaskJob(Task task) {
        try {
            schedulingService.rescheduleTask(task);
            logger.info("RESCHEDULE_JOB | Job rescheduled successfully: taskId={}", task.getId());
        } catch (SchedulingException | InvalidInputException e) {
            logger.error("RESCHEDULE_JOB | Rescheduling failed, rolling back: taskId={}", task.getId());
            throw e;
        }
    }

    /**
     * Internal record to hold calculated scheduling information
     */
    private record SchedulingData(
            ZonedDateTime nextReminder,
            String cronExpression,
            String clientTimezone
    ) {}
}
