package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.dto.outgoing.DetailedReminderResponse;
import com.sulaimaan.ReminderApp.dto.outgoing.PendingReminderResponse;
import com.sulaimaan.ReminderApp.entity.Reminder;
import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import com.sulaimaan.ReminderApp.repository.ReminderRepository;
import com.sulaimaan.ReminderApp.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);

    private final ReminderRepository reminderRepository;
    private final TaskRepository taskRepository;

    public ReminderService(ReminderRepository reminderRepository, TaskRepository taskRepository) {
        this.reminderRepository = reminderRepository;
        this.taskRepository = taskRepository;
    }

    public List<PendingReminderResponse> getLatestIncompleteByDevice(Long deviceId) {
        logger.info("ReminderService.getLatestIncompleteByDevice | deviceId={}", deviceId);
        List<Reminder> reminders = reminderRepository.findLatestIncompleteByDevice(deviceId);
        logger.info("ReminderService.getLatestIncompleteByDevice | deviceId={} count={}", deviceId, reminders.size());
        return reminders.stream().map(PendingReminderResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public Reminder createReminderInstance(Long taskId) {
        logger.info("ReminderService.createReminderInstance | taskId={}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.error("ReminderService.createReminderInstance | Task not found, id={}", taskId);
                    return new InvalidInputException("Task not found with id: " + taskId + " for creating reminder instance.");
                });

        Reminder reminder = new Reminder();
        reminder.setTask(task);
        reminder.setRemindedAt(ZonedDateTime.now(ZoneOffset.UTC));
        reminder.setCompleted(false);

        Reminder savedReminder = reminderRepository.save(reminder);
        logger.info("ReminderService.createReminderInstance | Saved Reminder id={}, taskId={}", savedReminder.getId(), taskId);
        return savedReminder;
    }

    public List<DetailedReminderResponse> getLatestIncompleteReminders(Long deviceId) {
        logger.info("📋 ReminderService.getLatestIncompleteReminders | deviceId={}", deviceId);
        List<DetailedReminderResponse> reminders = reminderRepository.findLatestIncompleteRemindersByDeviceId(deviceId);
        logger.info("✅ ReminderService.getLatestIncompleteReminders | deviceId={} count={}", deviceId, reminders.size());
        return reminders;
    }
}
