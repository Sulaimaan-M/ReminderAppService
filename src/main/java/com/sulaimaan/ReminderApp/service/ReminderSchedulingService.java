// com.sulaimaan.ReminderApp.service.ReminderSchedulingService.java
package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.entity.Reminder;
import com.sulaimaan.ReminderApp.quartz.job.ReminderJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.Date;

@Service
public class ReminderSchedulingService {

    private final Scheduler scheduler;

    @Autowired
    public ReminderSchedulingService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Schedules a Quartz job to trigger at the reminder's remindAt time.
     * Includes FCM token and message in JobDataMap for the job to use.
     */
    // In ReminderSchedulingService.java
    public void scheduleReminder(Reminder reminder) {
        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("message", reminder.getText());
            jobDataMap.put("reminderId", reminder.getId());

            // 👇 Get FCM token from DeviceToken (if linked)
            if (reminder.getDeviceToken() != null) {
                jobDataMap.put("fcmToken", reminder.getDeviceToken().getFcmToken());
            }

            JobDetail job = JobBuilder.newJob(ReminderJob.class)
                    .withIdentity("reminder-job-" + reminder.getId())
                    .usingJobData(jobDataMap)
                    .build();

            Date fireTime = Date.from(
                    reminder.getRemindAt().withZoneSameInstant(ZoneOffset.UTC).toInstant()
            );

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("reminder-trigger-" + reminder.getId())
                    .startAt(fireTime)
                    .build();

            scheduler.scheduleJob(job, trigger);
            System.out.println("✅ Scheduled job for reminder ID: " + reminder.getId());

        } catch (SchedulerException e) {
            throw new RuntimeException("Scheduling failed for reminder ID: " + reminder.getId(), e);
        }
    }

    /**
     * Optional: Cancel a scheduled reminder
     */
    public void unscheduleReminder(Long reminderId) {
        try {
            JobKey jobKey = JobKey.jobKey("reminder-job-" + reminderId);
            scheduler.deleteJob(jobKey);
            System.out.println("🗑️ Unscheduled reminder job: " + reminderId);
        } catch (SchedulerException e) {
            System.err.println("❌ Failed to unschedule job: " + e.getMessage());
        }
    }
}
