package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.exception_handling.exception.SchedulingException;
import com.sulaimaan.ReminderApp.helper.RecurrenceType;
import com.sulaimaan.ReminderApp.quartz.ReminderNotificationJob;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.TimeZone;

@Service
public class SchedulingService {

    private final Scheduler scheduler;

    public SchedulingService(Scheduler scheduler) {
        this.scheduler = scheduler;
        System.out.println("✅ SchedulingService initialized with Scheduler: " + scheduler);
    }

    public void scheduleTask(Task task) {
        System.out.println("🔧 Attempting to schedule task: " + task.getId());
        System.out.println("📋 Task type: " + task.getRecurrenceType());
        System.out.println("⏰ Next reminder at: " + task.getNextReminderAt());
        System.out.println("🕐 Cron expression: " + task.getCronExpression());

        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("taskId", task.getId());
            jobDataMap.put("taskText", task.getTaskTxt());
            jobDataMap.put("fcmToken", task.getDeviceToken().getFcmToken());

            JobDetail jobDetail = JobBuilder.newJob(ReminderNotificationJob.class)
                    .withIdentity("task-" + task.getId(), "reminder-jobs")
                    .usingJobData(jobDataMap)
                    .storeDurably()
                    .build();

            Trigger trigger;

            if (task.getRecurrenceType() == RecurrenceType.SIMPLE) {
                Date fireTime = Date.from(task.getNextReminderAt().toInstant());

                trigger = TriggerBuilder.newTrigger()
                        .withIdentity("trigger-" + task.getId(), "reminder-triggers")
                        .startAt(fireTime)
                        .build();

                System.out.println("📅 Using SimpleTrigger for SIMPLE task");
                System.out.println("🎯 Fire time: " + fireTime);
            } else {
                CronScheduleBuilder cronSchedule = CronScheduleBuilder
                        .cronSchedule(task.getCronExpression())
                        .inTimeZone(TimeZone.getTimeZone("UTC"));

                trigger = TriggerBuilder.newTrigger()
                        .withIdentity("trigger-" + task.getId(), "reminder-triggers")
                        .withSchedule(cronSchedule)
                        .build();

                System.out.println("⏰ Using CronTrigger with expression: " + task.getCronExpression());
                System.out.println("🌍 Timezone: UTC");
            }

            scheduler.scheduleJob(jobDetail, trigger);

            System.out.println("✅ Successfully scheduled task " + task.getId());

        } catch (SchedulerException e) {
            System.err.println("❌ Failed to schedule task " + task.getId() + ": " + e.getMessage());
            e.printStackTrace();
            throw new SchedulingException("Failed to schedule task: " + task.getId(), e);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error scheduling task " + task.getId() + ": " + e.getMessage());
            e.printStackTrace();
            throw new SchedulingException("Unexpected error scheduling task: " + task.getId(), e);
        }
    }

    public void rescheduleTask(Task task) {
        System.out.println("🔧 Attempting to reschedule task: " + task.getId());

        try {
            unscheduleTask(task.getId());
            scheduleTask(task);

            System.out.println("✅ Successfully rescheduled task " + task.getId());

        } catch (Exception e) {
            System.err.println("❌ Failed to reschedule task " + task.getId() + ": " + e.getMessage());
            e.printStackTrace();
            throw new SchedulingException("Failed to reschedule task: " + task.getId(), e);
        }
    }

    public void unscheduleTask(Long taskId) {
        System.out.println("🔧 Attempting to unschedule task: " + taskId);

        try {
            JobKey jobKey = new JobKey("task-" + taskId, "reminder-jobs");
            boolean deleted = scheduler.deleteJob(jobKey);

            if (deleted) {
                System.out.println("✅ Successfully unscheduled task " + taskId);
            } else {
                System.out.println("⚠️ Task " + taskId + " was not scheduled (or already removed)");
            }

        } catch (SchedulerException e) {
            System.err.println("❌ Failed to unschedule task " + taskId + ": " + e.getMessage());
            e.printStackTrace();
            throw new SchedulingException("Failed to unschedule task: " + taskId, e);
        }
    }
}
