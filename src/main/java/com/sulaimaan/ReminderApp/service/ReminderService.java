package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.dto.outgoing.DetailedReminderResponse;
import com.sulaimaan.ReminderApp.entity.Reminder;
import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import com.sulaimaan.ReminderApp.helper.RecurrenceType;
import com.sulaimaan.ReminderApp.repository.ReminderRepository;
import com.sulaimaan.ReminderApp.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing reminder instances and their completion status
 */
@Service
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);

    private final ReminderRepository reminderRepository;
    private final TaskRepository taskRepository;

    public ReminderService(ReminderRepository reminderRepository, TaskRepository taskRepository) {
        this.reminderRepository = reminderRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Retrieves the latest incomplete reminder for each recurring task of a device
     */
    public List<DetailedReminderResponse> getLatestIncompleteReminders(Long deviceId) {
        logger.info("📋 ReminderService.getLatestIncompleteReminders | deviceId={}", deviceId);
        List<DetailedReminderResponse> reminders = reminderRepository.findLatestIncompleteRemindersByDeviceId(deviceId);
        logger.info("✅ ReminderService.getLatestIncompleteReminders | deviceId={} count={}", deviceId, reminders.size());
        return reminders;
    }

    /**
     * Retrieves all reminders associated with a specific task
     */
    public List<DetailedReminderResponse> getRemindersByTaskId(Long taskId) {
        logger.info("📋 ReminderService.getRemindersByTaskId | taskId={}", taskId);
        List<DetailedReminderResponse> reminders = reminderRepository.findRemindersByTaskId(taskId);
        logger.info("✅ ReminderService.getRemindersByTaskId | taskId={} count={}", taskId, reminders.size());
        return reminders;
    }

    /**
     * Creates a new reminder instance for a task when it is triggered
     */
    @Transactional
    public Reminder createReminderInstance(Long taskId) {
        logger.info("🔔 ReminderService.createReminderInstance | taskId={}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.error("ReminderService.createReminderInstance | Task not found, id={}", taskId);
                    return new InvalidInputException("Task not found with id: " + taskId + " for creating reminder instance.");
                });

        Reminder reminder = new Reminder();
        reminder.setTask(task);
        reminder.setRemindedAt(ZonedDateTime.now(ZoneOffset.UTC));
        reminder.setIsCompleted(false);

        Reminder savedReminder = reminderRepository.save(reminder);
        logger.info("🔔 ReminderService.createReminderInstance | Saved Reminder id={}, taskId={}", savedReminder.getId(), taskId);
        return savedReminder;
    }

    /**
     * Completes a reminder by deleting it; for SIMPLE tasks, deletes both reminder and task
     */
    @Transactional
    public boolean completeReminder(Long reminderId) {
        logger.info("🔔 ReminderService.completeReminder | reminderId={}", reminderId);

        Optional<Reminder> reminderOptional = reminderRepository.findById(reminderId);
        if (reminderOptional.isEmpty()) {
            logger.warn("⚠️ ReminderService.completeReminder | Reminder not found, id={}", reminderId);
            return false;
        }

        Reminder reminder = reminderOptional.get();

        Task task = reminder.getTask();
        if (task == null) {
            logger.error("❌ ReminderService.completeReminder | Reminder has no associated task, id={}", reminderId);
            throw new InvalidInputException("Reminder has no associated task: " + reminderId);
        }

        logger.info("🔍 ReminderService.completeReminder | Task type={}, reminderId={}, taskId={}",
                task.getRecurrenceType(), reminderId, task.getId());

        if (task.getRecurrenceType() == RecurrenceType.SIMPLE) {
            logger.info("🗑️ ReminderService.completeReminder | SIMPLE task - deleting task {} and reminder {}",
                    task.getId(), reminderId);
            taskRepository.delete(task);
        } else {
            logger.info("🗑️ ReminderService.completeReminder | Recurring task - deleting reminder {}", reminderId);
            reminderRepository.delete(reminder);
        }

        return true;
    }
}
