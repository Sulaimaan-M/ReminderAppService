package com.sulaimaan.ReminderApp.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReminderNotificationJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ReminderNotificationJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long taskId = context.getJobDetail().getJobDataMap().getLong("taskId");
        String taskText = context.getJobDetail().getJobDataMap().getString("taskText");
        String fcmToken = context.getJobDetail().getJobDataMap().getString("fcmToken");

        logger.info("🔔 ReminderNotificationJob | firing taskId={} task='{}' tokenLen={}",
                taskId, taskText, fcmToken == null ? 0 : fcmToken.length());

        // TODO:
        // 1) Send FCM notification using fcmToken
        // 2) Persist Reminder instance (remindedAt=now, isCompleted=false)
        // 3) For recurring tasks, optionally compute and update Task.nextReminderAt to next

        logger.info("🔔 ReminderNotificationJob | done taskId={}", taskId);
    }
}
