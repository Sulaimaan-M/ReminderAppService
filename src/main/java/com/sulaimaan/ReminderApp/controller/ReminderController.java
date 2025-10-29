package com.sulaimaan.ReminderApp.controller;

import com.sulaimaan.ReminderApp.dto.outgoing.DetailedReminderResponse;
import com.sulaimaan.ReminderApp.service.ReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing reminders
 */
@CrossOrigin
@RestController
@RequestMapping("/reminder")
public class ReminderController {

    private static final Logger logger = LoggerFactory.getLogger(ReminderController.class);

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * Retrieves all pending (incomplete) reminders for a specific device
     */
    @GetMapping("/device/{deviceId}/pending")
    public List<DetailedReminderResponse> getLatestIncompleteByDevice(@PathVariable Long deviceId) {
        logger.info("Fetching pending reminders for device ID: {}", deviceId);

        List<DetailedReminderResponse> res = reminderService.getLatestIncompleteReminders(deviceId);

        logger.info("Retrieved {} pending reminders for device ID: {}", res.size(), deviceId);
        return res;
    }

    /**
     * Retrieves all reminders associated with a specific task
     */
    @GetMapping("/task/{taskId}")
    public List<DetailedReminderResponse> getRemindersByTask(@PathVariable Long taskId) {
        logger.info("Fetching all reminders for task ID: {}", taskId);

        List<DetailedReminderResponse> reminders = reminderService.getRemindersByTaskId(taskId);

        logger.info("Retrieved {} reminders for task ID: {}", reminders.size(), taskId);
        return reminders;
    }

    /**
     * Marks a reminder as completed
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> completeReminder(@PathVariable Long id) {
        logger.info("Attempting to mark reminder ID {} as complete", id);

        try {
            boolean success = reminderService.completeReminder(id);

            if (success) {
                logger.info("Reminder ID {} marked as complete successfully", id);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("Reminder ID {} not found or already completed", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Failed to complete reminder ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
