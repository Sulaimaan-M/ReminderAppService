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

@Service
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);

    private final ReminderRepository reminderRepository;
    private final TaskRepository taskRepository;

    public ReminderService(ReminderRepository reminderRepository, TaskRepository taskRepository) {
        this.reminderRepository = reminderRepository;
        this.taskRepository = taskRepository;
    }

    public List<DetailedReminderResponse> getLatestIncompleteReminders(Long deviceId) {
        logger.info("📋 ReminderService.getLatestIncompleteReminders | deviceId={}", deviceId);
        List<DetailedReminderResponse> reminders = reminderRepository.findLatestIncompleteRemindersByDeviceId(deviceId);
        logger.info("✅ ReminderService.getLatestIncompleteReminders | deviceId={} count={}", deviceId, reminders.size());
        return reminders;
    }

    @Transactional
    public void createReminderInstance(Long taskId) {
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
    }

    /**
     * Complete a reminder based on business logic:
     * - If task is SIMPLE → Delete both reminder and task
     * - If task is recurring → Set reminder.isCompleted = true
     *
     * @param reminderId ID of the reminder to complete
     * @return true if successful, false if reminder not found
     */
    @Transactional
    public boolean completeReminder(Long reminderId) {
        logger.info("🔔 ReminderService.completeReminder | reminderId={}", reminderId);

        // Fetch the reminder by ID
        Optional<Reminder> reminderOptional = reminderRepository.findById(reminderId);
        if (reminderOptional.isEmpty()) {
            logger.warn("⚠️ ReminderService.completeReminder | Reminder not found, id={}", reminderId);
            return false;
        }

        Reminder reminder = reminderOptional.get();

        // Check if already completed
        if (reminder.getIsCompleted()) {
            logger.info("ℹ️ ReminderService.completeReminder | Reminder already completed, id={}", reminderId);
            return true; // Already completed, consider it success
        }

        // Get the associated task
        Task task = reminder.getTask();
        if (task == null) {
            logger.error("❌ ReminderService.completeReminder | Reminder has no associated task, id={}", reminderId);
            throw new InvalidInputException("Reminder has no associated task: " + reminderId);
        }

        logger.info("🔍 ReminderService.completeReminder | Task type={}, reminderId={}, taskId={}",
                task.getRecurrenceType(), reminderId, task.getId());

        // Business logic: Check if task is SIMPLE
        if (task.getRecurrenceType() == RecurrenceType.SIMPLE) {
            // Delete both reminder and task
            logger.info("🗑️ ReminderService.completeReminder | SIMPLE task - deleting task {} and reminder {}",
                    task.getId(), reminderId);
            taskRepository.delete(task);
            // Reminder will be cascade deleted, but let's be explicit
            reminderRepository.delete(reminder);
        } else {
            // Update reminder status to completed
            logger.info("✏️ ReminderService.completeReminder | Recurring task - marking reminder {} as completed",
                    reminderId);
            reminder.setIsCompleted(true);
            reminderRepository.save(reminder);
        }

        return true;
    }
}
