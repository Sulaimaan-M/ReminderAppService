package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.dto.ReminderDTO;
import com.sulaimaan.ReminderApp.entity.DeviceToken;
import com.sulaimaan.ReminderApp.entity.Reminder;
import com.sulaimaan.ReminderApp.quartz.QuartzReminderScheduler;
import com.sulaimaan.ReminderApp.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ReminderService {

    private final ReminderRepository repo;
    private final QuartzReminderScheduler quartzScheduler;
    private final DeviceTokenService deviceTokenService;

    @Autowired
    public ReminderService(
            ReminderRepository repo,
            QuartzReminderScheduler quartzScheduler,
            DeviceTokenService deviceTokenService) {
        this.repo = repo;
        this.quartzScheduler = quartzScheduler;
        this.deviceTokenService = deviceTokenService;
    }

    public Reminder createReminder(ReminderDTO reminderDTO) {
        System.out.println("🔍 [ReminderService] Starting createReminder");
        try {
            if (!isValidReminderTime(reminderDTO.remindAt)) {
                System.out.println("❌ [ReminderService] Validation failed");
                return null;
            }

            Reminder reminder = new Reminder();
            reminder.setText(reminderDTO.reminderTxt);
            reminder.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
            reminder.setRemindAt(reminderDTO.remindAt.withZoneSameInstant(ZoneOffset.UTC));
            reminder.setIntervalType(reminderDTO.interval);

            System.out.println("🔍 [ReminderService] Created reminder entity. Interval: " + reminderDTO.interval);
            return saveAndSchedule(reminder, reminderDTO.deviceTokenId);

        } catch (Exception e) {
            logError("Error creating reminder", e);
            return null;
        }
    }

    public Reminder updateReminder(Long id, ReminderDTO dto) {
        System.out.println("🔍 [ReminderService] Starting updateReminder for ID: " + id);
        try {
            Reminder reminder = repo.findById(id).orElse(null);
            if (reminder == null) {
                System.out.println("❌ [ReminderService] Reminder not found: " + id);
                return null;
            }

            if (!isValidReminderTime(dto.remindAt)) {
                System.out.println("❌ [ReminderService] Validation failed for update");
                return null;
            }

            unscheduleReminder(id);
            reminder.setText(dto.reminderTxt);
            reminder.setRemindAt(dto.remindAt.withZoneSameInstant(ZoneOffset.UTC));
            reminder.setIntervalType(dto.interval);

            System.out.println("🔍 [ReminderService] Updated reminder entity. Interval: " + dto.interval);
            return saveAndSchedule(reminder, dto.deviceTokenId);

        } catch (Exception e) {
            logError("Error updating reminder", e);
            return null;
        }
    }

    public boolean deleteReminder(Long id) {
        System.out.println("🔍 [ReminderService] Starting deleteReminder for ID: " + id);
        try {
            unscheduleReminder(id);
            repo.deleteById(id);
            System.out.println("✅ [ReminderService] Deleted reminder ID: " + id);
            return true;
        } catch (Exception e) {
            logError("Error deleting reminder", e);
            return false;
        }
    }

    public List<Reminder> getRemindersByDeviceTokenId(Long deviceTokenId) {
        System.out.println("🔍 [ReminderService] Fetching reminders for deviceTokenId: " + deviceTokenId);
        return repo.findByDeviceTokenId(deviceTokenId);
    }

    private Reminder saveAndSchedule(Reminder reminder, Long deviceTokenId) {
        try {
            if (deviceTokenId != null) {
                DeviceToken deviceToken = deviceTokenService.getDeviceTokenById(deviceTokenId);
                if (deviceToken == null) {
                    System.out.println("❌ [ReminderService] DeviceToken not found: " + deviceTokenId);
                    return null;
                }
                reminder.setDeviceToken(deviceToken);
                System.out.println("🔍 [ReminderService] Linked DeviceToken ID: " + deviceTokenId);
            }

            reminder = repo.save(reminder);
            System.out.println("✅ [ReminderService] Saved reminder to DB. ID: " + reminder.getId());

            quartzScheduler.schedule(reminder);
            System.out.println("✅ [ReminderService] Called quartzScheduler.schedule()");
            return reminder;

        } catch (Exception e) {
            logError("Error in saveAndSchedule", e);
            return null;
        }
    }

    private void unscheduleReminder(Long reminderId) {
        try {
            org.quartz.Scheduler scheduler = quartzScheduler.getScheduler();
            org.quartz.JobKey jobKey = org.quartz.JobKey.jobKey(reminderId.toString());
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                System.out.println("🗑️ [ReminderService] Unscheduled job: " + reminderId);
            } else {
                System.out.println("ℹ️ [ReminderService] Job not found for unscheduling: " + reminderId);
            }
        } catch (Exception e) {
            System.err.println("❌ [ReminderService] Failed to unschedule job: " + e.getMessage());
        }
    }

    private boolean isValidReminderTime(ZonedDateTime remindAt) {
        if (remindAt == null) {
            System.out.println("❌ [ReminderService] remindAt is null");
            return false;
        }
        ZonedDateTime now = ZonedDateTime.now();
        System.out.println("🔍 [ReminderService] Validation - remindAt: " + remindAt + ", now: " + now);
        if (remindAt.isBefore(now)) {
            System.out.println("❌ [ReminderService] remindAt is in the past!");
            return false;
        }
        // Add 2-second buffer
        if (remindAt.isBefore(now.plusSeconds(2))) {
            System.out.println("⚠️ [ReminderService] remindAt is too close! Adding 2-second buffer.");
            // We don't adjust here - just warn. Scheduler will handle.
        }
        return true;
    }

    private void logError(String message, Exception e) {
        System.err.println("❌ [ReminderService] " + message + ": " + e.getMessage());
        e.printStackTrace();
    }
}
