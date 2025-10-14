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
    public void scheduleReminder(Reminder reminder) {
        try {
            // Build JobDataMap with everything the job needs
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("message", reminder.getText());
            //jobDataMap.put("fcmToken", reminder.getFcmToken()); // or deviceToken, etc.
            jobDataMap.put("reminderId", reminder.getId());

            // Create job
            JobDetail job = JobBuilder.newJob(ReminderJob.class)
                    .withIdentity("reminder-job-" + reminder.getId())
                    .usingJobData(jobDataMap)
                    .build();

            // Convert remindAt (ZonedDateTime) to Date for Quartz
            Date fireTime = Date.from(
                    reminder.getRemindAt()
                            .withZoneSameInstant(ZoneOffset.UTC)
                            .toInstant()
            );

            // Create trigger
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("reminder-trigger-" + reminder.getId())
                    .startAt(fireTime)
                    .build();

            // Schedule
            scheduler.scheduleJob(job, trigger);
            System.out.println("✅ Scheduled reminder job for ID: " + reminder.getId() +
                    " at " + reminder.getRemindAt());

        } catch (SchedulerException e) {
            System.err.println("❌ Failed to schedule reminder job: " + e.getMessage());
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
