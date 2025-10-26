package com.sulaimaan.ReminderApp.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ReminderNotificationJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long taskId = context.getJobDetail().getJobDataMap().getLong("taskId");
        String taskText = context.getJobDetail().getJobDataMap().getString("taskText");
        String fcmToken = context.getJobDetail().getJobDataMap().getString("fcmToken");

        System.out.println("🔔 Reminder triggered for task ID: " + taskId);
        System.out.println("📝 Task: " + taskText);
        System.out.println("📱 Sending to device: " + fcmToken);

        // TODO: Notification service will be implemented here
        // 1. Send FCM notification using fcmToken
        // 2. Create Reminder entry in database with remindedAt = now, isCompleted = false
        // 3. Update Task.nextReminderAt to next occurrence
    }
}
