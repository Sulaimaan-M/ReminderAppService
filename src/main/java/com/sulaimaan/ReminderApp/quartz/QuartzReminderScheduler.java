package com.sulaimaan.ReminderApp.quartz;

import com.sulaimaan.ReminderApp.entity.Reminder;
import com.sulaimaan.ReminderApp.helper.CronStringBuilder;
import com.sulaimaan.ReminderApp.helper.IntervalType;
import com.sulaimaan.ReminderApp.quartz.job.ReminderJob;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class QuartzReminderScheduler {

    private final Scheduler scheduler;

    public QuartzReminderScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public void schedule(Reminder reminder) {
        System.out.println("🔍 [QuartzScheduler] Starting schedule for reminder ID: " + reminder.getId());
        if (reminder.getId() == null) {
            throw new IllegalArgumentException("Reminder must be persisted (have an ID) before scheduling");
        }

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("message", reminder.getText());
        if (reminder.getDeviceToken() != null) {
            jobDataMap.put("fcmToken", reminder.getDeviceToken().getFcmToken());
            System.out.println("🔍 [QuartzScheduler] Added FCM token to JobDataMap");
        } else {
            System.out.println("⚠️ [QuartzScheduler] No DeviceToken linked to reminder");
        }

        JobDetail jobDetail = JobBuilder.newJob(ReminderJob.class)
                .withIdentity(reminder.getId().toString())
                .usingJobData(jobDataMap)
                .storeDurably(false)
                .build();

        Trigger trigger;
        System.out.println("🔍 [QuartzScheduler] Interval type: " + reminder.getIntervalType());

        if (reminder.getIntervalType() == IntervalType.SIMPLE) {
            Date fireTime = Date.from(reminder.getRemindAt().toInstant());
            System.out.println("⏰ [QuartzScheduler] Scheduling SIMPLE reminder for UTC time: " + fireTime);
            System.out.println("⏰ [QuartzScheduler] Current UTC time: " + new Date());

            // FIX: Added SimpleScheduleBuilder and forJob
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(reminder.getId().toString() + "_trigger")
                    .forJob(jobDetail)
                    .startAt(fireTime)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withMisfireHandlingInstructionFireNow())
                    .build();
        } else {
            String cronExpression = CronStringBuilder.build(reminder.getRemindAt(), reminder.getIntervalType());
            System.out.println("⏰ [QuartzScheduler] Scheduling RECURRING reminder with cron: " + cronExpression);

            // FIX: Added trigger identity and misfire handling
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(reminder.getId().toString() + "_trigger")
                    .forJob(jobDetail)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                            .withMisfireHandlingInstructionFireAndProceed())
                    .build();
        }

        try {
            scheduler.scheduleJob(jobDetail, trigger);
            System.out.println("✅ [QuartzScheduler] Successfully scheduled job ID: " + reminder.getId());

            // Verify job exists
            JobKey jobKey = JobKey.jobKey(reminder.getId().toString());
            if (scheduler.checkExists(jobKey)) {
                System.out.println("✅ [QuartzScheduler] Verified job exists in scheduler");

                // Also check next fire time
                TriggerKey triggerKey = TriggerKey.triggerKey(reminder.getId().toString() + "_trigger");
                Trigger scheduledTrigger = scheduler.getTrigger(triggerKey);
                if (scheduledTrigger != null) {
                    System.out.println("⏰ [QuartzScheduler] Next fire time: " + scheduledTrigger.getNextFireTime());
                }
            } else {
                System.out.println("❌ [QuartzScheduler] Job NOT found after scheduling!");
            }

        } catch (SchedulerException e) {
            System.err.println("❌ [QuartzScheduler] Failed to schedule job: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Scheduling failed for reminder: " + reminder.getId(), e);
        }
    }
}
