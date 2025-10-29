package com.sulaimaan.ReminderApp.quartz;

import com.sulaimaan.ReminderApp.service.NotificationService;
import com.sulaimaan.ReminderApp.service.ReminderService;
import com.sulaimaan.ReminderApp.service.TaskService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Quartz job that executes when a scheduled reminder is triggered, sends push notification and persists reminder instance
 */
@Component
public class ReminderNotificationJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ReminderNotificationJob.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private TaskService taskService;

    /**
     * Executes the reminder job by sending FCM notification, creating reminder instance, and updating next reminder time
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        Long taskId = jobDataMap.getLong("taskId");
        String taskText = jobDataMap.getString("taskText");
        String fcmToken = jobDataMap.getString("fcmToken");

        ZonedDateTime executionTimeUtc = ZonedDateTime.now(ZoneOffset.UTC);

        logger.info("🔔 ReminderNotificationJob | Firing taskId={} task='{}' at UTC {}",
                taskId, taskText, executionTimeUtc);

        try {
            if (fcmToken != null && !fcmToken.isEmpty()) {
                notificationService.sendPushNotification(fcmToken, taskText);
            } else {
                logger.warn("🔔 ReminderNotificationJob | No FCM token found for taskId={}, cannot send notification.", taskId);
            }

            reminderService.createReminderInstance(taskId);

            taskService.updateNextReminderTime(taskId, executionTimeUtc);

            logger.info("🔔 ReminderNotificationJob | Done taskId={}", taskId);

        } catch (Exception e) {
            logger.error("🔔 ReminderNotificationJob | Failed processing taskId={}. Error: {}", taskId, e.getMessage(), e);
        }
    }
}
