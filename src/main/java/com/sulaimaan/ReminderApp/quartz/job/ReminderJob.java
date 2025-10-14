package com.sulaimaan.ReminderApp.quartz.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class ReminderJob implements Job {

    // Static reference to Spring context (needed because Quartz creates this job, not Spring)
    private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        ReminderJob.applicationContext = context;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
            String message = data.getString("message");

            System.out.println("🔔 Reminder triggered: " + message);
            System.out.println("🕒 Executed at: " + ZonedDateTime.now());

            // TODO: Get FCM token from JobDataMap once we start saving it in reminders
            // For now, we skip sending notification because we don't have a token yet.
            // Later, this will be: String fcmToken = data.getString("fcmToken");
            // And then: notificationService.sendPushNotification(fcmToken, message);

            System.out.println("ℹ️ Skipping push notification (FCM token not available yet).");

        } catch (Exception e) {
            System.out.println("❌ Error in ReminderJob: " + e.getMessage());
            throw new JobExecutionException(e);
        }
    }
}
