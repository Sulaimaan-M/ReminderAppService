package com.sulaimaan.ReminderApp.quartz;

import com.sulaimaan.ReminderApp.service.NotificationService; // Import NotificationService
import com.sulaimaan.ReminderApp.service.ReminderService; // Import ReminderService
import com.sulaimaan.ReminderApp.service.TaskService; // Import TaskService
import org.quartz.Job;
import org.quartz.JobDataMap; // Import JobDataMap
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Import Autowired
import org.springframework.stereotype.Component; // Import Component

import java.time.ZoneOffset; // Import ZoneOffset
import java.time.ZonedDateTime; // Import ZonedDateTime

@Component // Make it a Spring component
public class ReminderNotificationJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ReminderNotificationJob.class);

    // Inject services using Autowired (enabled by AutowiringSpringBeanJobFactory)
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private TaskService taskService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        Long taskId = jobDataMap.getLong("taskId");
        String taskText = jobDataMap.getString("taskText");
        String fcmToken = jobDataMap.getString("fcmToken");

        // Use UTC time for consistency with scheduling and next time calculation
        ZonedDateTime executionTimeUtc = ZonedDateTime.now(ZoneOffset.UTC);

        logger.info("🔔 ReminderNotificationJob | Firing taskId={} task='{}' at UTC {}",
                taskId, taskText, executionTimeUtc);

        try {
            // 1) Send FCM notification using fcmToken
            if (fcmToken != null && !fcmToken.isEmpty()) {
                notificationService.sendPushNotification(fcmToken, taskText);
            } else {
                logger.warn("🔔 ReminderNotificationJob | No FCM token found for taskId={}, cannot send notification.", taskId);
            }

            // 2) Persist Reminder instance (remindedAt=executionTimeUtc, isCompleted=false)
            // This is now handled by ReminderService
            reminderService.createReminderInstance(taskId);

            // 3) For recurring tasks, compute and update Task.nextReminderAt to next
            // This is now handled by TaskService
            taskService.updateNextReminderTime(taskId, executionTimeUtc);

            logger.info("🔔 ReminderNotificationJob | Done taskId={}", taskId);

        } catch (Exception e) {
            // Log error but don't throw JobExecutionException unless you want Quartz to handle retries/failures
            logger.error("🔔 ReminderNotificationJob | Failed processing taskId={}. Error: {}", taskId, e.getMessage(), e);
            // Consider throwing new JobExecutionException(e, false); // false = don't refire immediately
        }
    }
}
