package com.sulaimaan.ReminderApp.controller;

import com.sulaimaan.ReminderApp.dto.outgoing.DetailedReminderResponse;
import com.sulaimaan.ReminderApp.service.ReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/reminder")
public class ReminderController {

    private static final Logger logger = LoggerFactory.getLogger(ReminderController.class);

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping("/device/{deviceId}/pending")
    public List<DetailedReminderResponse> getLatestIncompleteByDevice(@PathVariable Long deviceId) {
        logger.info("📋 GET /reminder/device/{}/pending", deviceId);
        List<DetailedReminderResponse> res = reminderService.getLatestIncompleteReminders(deviceId);
        logger.info("✅ GET /reminder/device/{}/pending | Returning {} reminders", deviceId, res.size());
        return res;
    }

    // NEW ENDPOINT: Get all reminders by task ID
    @GetMapping("/task/{taskId}")
    public List<DetailedReminderResponse> getRemindersByTask(@PathVariable Long taskId) {
        logger.info("📋 GET /reminder/task/{}", taskId);
        List<DetailedReminderResponse> reminders = reminderService.getRemindersByTaskId(taskId);
        logger.info("✅ GET /reminder/task/{} | Returning {} reminders", taskId, reminders.size());
        return reminders;
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> completeReminder(@PathVariable Long id) {
        logger.info("✅ PUT /reminder/{}/complete", id);
        try {
            boolean success = reminderService.completeReminder(id);
            if (success) {
                logger.info("✅ PUT /reminder/{}/complete | Reminder completed successfully", id);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("⚠️ PUT /reminder/{}/complete | Reminder not found or already completed", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("❌ PUT /reminder/{}/complete | Error completing reminder: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
