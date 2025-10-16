// com.sulaimaan.ReminderApp.quartz.job.ReminderJob.java
package com.sulaimaan.ReminderApp.quartz.job;

import com.sulaimaan.ReminderApp.service.NotificationService;
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
            String fcmToken = data.getString("fcmToken");

            System.out.println("🔔 [ReminderJob] Reminder triggered: " + message);
            System.out.println("🕒 [ReminderJob] Executed at: " + ZonedDateTime.now());
            System.out.println("📱 [ReminderJob] FCM token: " + (fcmToken != null ? fcmToken.substring(0, Math.min(10, fcmToken.length())) + "..." : "null"));

            if (fcmToken != null && !fcmToken.trim().isEmpty()) {
                NotificationService notificationService =
                        applicationContext.getBean(NotificationService.class);
                notificationService.sendPushNotification(fcmToken, message);
                System.out.println("✅ [ReminderJob] Notification service called");
            } else {
                System.out.println("⚠️ [ReminderJob] No FCM token provided. Skipping push notification.");
            }

        } catch (Exception e) {
            System.out.println("❌ [ReminderJob] Error in ReminderJob: " + e.getMessage());
            e.printStackTrace();
            throw new JobExecutionException(e);
        }
        System.out.println("✅ [ReminderJob] Job execution completed");
    }
}
