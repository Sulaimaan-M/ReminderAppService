// com.sulaimaan.ReminderApp.service.ReminderService.java
package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.dto.ReminderDTO;
import com.sulaimaan.ReminderApp.entity.DeviceToken;
import com.sulaimaan.ReminderApp.entity.Reminder;
import com.sulaimaan.ReminderApp.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class ReminderService {

    private final ReminderRepository repo;
    private final ReminderSchedulingService schedulingService;
    private final DeviceTokenService deviceTokenService; // ← ADD THIS

    @Autowired
    public ReminderService(
            ReminderRepository repo,
            ReminderSchedulingService schedulingService,
            DeviceTokenService deviceTokenService) { // ← ADD THIS
        this.repo = repo;
        this.schedulingService = schedulingService;
        this.deviceTokenService = deviceTokenService; // ← ADD THIS
    }

    public boolean createReminder(ReminderDTO reminderDTO) {
        try {
            // Validate
            if (reminderDTO.remindAt == null) {
                System.out.println("❌ remindAt is required");
                return false;
            }
            if (reminderDTO.remindAt.isBefore(ZonedDateTime.now())) {
                System.out.println("❌ Reminder time must be in the future");
                return false;
            }

            Reminder reminder = new Reminder();
            reminder.setText(reminderDTO.reminderTxt);
            reminder.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
            reminder.setRemindAt(reminderDTO.remindAt.withZoneSameInstant(ZoneOffset.UTC));
            reminder.setIntervalType(reminderDTO.interval);

            // 👇 NEW: Handle FCM token → DeviceToken
            if (reminderDTO.fcmToken != null && !reminderDTO.fcmToken.trim().isEmpty()) {
                DeviceToken deviceToken = deviceTokenService.registerDevice(reminderDTO.fcmToken);
                reminder.setDeviceToken(deviceToken);
            }

            reminder = repo.save(reminder); // Now has ID

            schedulingService.scheduleReminder(reminder);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error creating reminder: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
