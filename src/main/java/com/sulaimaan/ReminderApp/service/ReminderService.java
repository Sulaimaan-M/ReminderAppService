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
import java.util.List;
import java.util.Optional;

@Service
public class ReminderService {

    private final ReminderRepository repo;
    private final ReminderSchedulingService schedulingService;
    private final DeviceTokenService deviceTokenService;

    @Autowired
    public ReminderService(
            ReminderRepository repo,
            ReminderSchedulingService schedulingService,
            DeviceTokenService deviceTokenService) {
        this.repo = repo;
        this.schedulingService = schedulingService;
        this.deviceTokenService = deviceTokenService;
    }

    // === CREATE ===
// In ReminderService.java
    public boolean createReminder(ReminderDTO reminderDTO) {
        try {
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

            // 👇 NEW: Use deviceTokenId to fetch DeviceToken
            if (reminderDTO.deviceTokenId != null) {
                DeviceToken deviceToken = deviceTokenService.getDeviceTokenById(reminderDTO.deviceTokenId);
                if (deviceToken == null) {
                    System.out.println("❌ DeviceToken not found with ID: " + reminderDTO.deviceTokenId);
                    return false;
                }
                reminder.setDeviceToken(deviceToken);
            }

            reminder = repo.save(reminder);
            schedulingService.scheduleReminder(reminder);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error creating reminder: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // === UPDATE (EDIT) ===
    // In ReminderService.java
    public boolean updateReminder(Long id, ReminderDTO dto) {
        try {
            Reminder reminder = repo.findById(id).orElse(null);
            if (reminder == null) {
                System.out.println("❌ Reminder not found: " + id);
                return false;
            }

            schedulingService.unscheduleReminder(id);

            reminder.setText(dto.reminderTxt);
            reminder.setRemindAt(dto.remindAt.withZoneSameInstant(ZoneOffset.UTC));
            reminder.setIntervalType(dto.interval);

            // 👇 Handle deviceTokenId (not FCM token string)
            if (dto.deviceTokenId != null) {
                DeviceToken deviceToken = deviceTokenService.getDeviceTokenById(dto.deviceTokenId);
                if (deviceToken == null) {
                    System.out.println("❌ DeviceToken not found with ID: " + dto.deviceTokenId);
                    return false;
                }
                reminder.setDeviceToken(deviceToken);
            }
            // If deviceTokenId is null, we keep the existing link (or leave it null)

            reminder = repo.save(reminder);
            schedulingService.scheduleReminder(reminder);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error updating reminder: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // === DELETE ===
    public boolean deleteReminder(Long id) {
        try {
            // 1. Cancel job first
            schedulingService.unscheduleReminder(id);

            // 2. Delete from database
            repo.deleteById(id);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error deleting reminder: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // In ReminderService.java
    public List<Reminder> getRemindersByToken(String fcmToken) {
        return repo.findByDeviceToken_FcmToken(fcmToken);
    }

}
