package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.exception_handling.exception.SchedulingException;
import com.sulaimaan.ReminderApp.helper.RecurrenceType;
import com.sulaimaan.ReminderApp.quartz.ReminderNotificationJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.TimeZone;

@Service
public class SchedulingService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingService.class);

    private final Scheduler scheduler;

    public SchedulingService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void scheduleTask(Task task) {
        logger.info("🗓️ SchedulingService.scheduleTask | taskId={} type={} nextAt={} cron='{}'",
                task.getId(), task.getRecurrenceType(), task.getNextReminderAt(), task.getCronExpression());

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
                logger.info("🗓️ SchedulingService.scheduleTask | Using SimpleTrigger at {}", fireTime);
            } else {
                CronScheduleBuilder cronSchedule = CronScheduleBuilder
                        .cronSchedule(task.getCronExpression())
                        .inTimeZone(TimeZone.getTimeZone("UTC"));
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity("trigger-" + task.getId(), "reminder-triggers")
                        .withSchedule(cronSchedule)
                        .build();
                logger.info("🗓️ SchedulingService.scheduleTask | Using CronTrigger (UTC)");
            }

            scheduler.scheduleJob(jobDetail, trigger);
            logger.info("✅ SchedulingService.scheduleTask | Job scheduled successfully | taskId={}", task.getId());
        } catch (Exception e) {
            logger.error("❌ SchedulingService.scheduleTask | Scheduling failed | taskId={}", task.getId(), e);
            throw new SchedulingException("Failed to schedule task: " + task.getId(), e);
        }
    }

    public void rescheduleTask(Task task) {
        logger.info("🔄 SchedulingService.rescheduleTask | taskId={}", task.getId());
        try {
            unscheduleTask(task.getId());
            scheduleTask(task);
            logger.info("✅ SchedulingService.rescheduleTask | Reschedule successful | taskId={}", task.getId());
        } catch (Exception e) {
            logger.error("❌ SchedulingService.rescheduleTask | Reschedule failed | taskId={}", task.getId(), e);
            throw new SchedulingException("Failed to reschedule task: " + task.getId(), e);
        }
    }

    public void unscheduleTask(Long taskId) {
        logger.info("🗑️ SchedulingService.unscheduleTask | taskId={}", taskId);
        try {
            JobKey jobKey = new JobKey("task-" + taskId, "reminder-jobs");
            boolean deleted = scheduler.deleteJob(jobKey);
            if (deleted) {
                logger.info("✅ SchedulingService.unscheduleTask | Job unscheduled successfully | taskId={}", taskId);
            } else {
                logger.warn("⚠️ SchedulingService.unscheduleTask | Job was not found to unschedule | taskId={}", taskId);
            }
        } catch (SchedulerException e) {
            logger.error("❌ SchedulingService.unscheduleTask | Unschedule failed | taskId={}", taskId, e);
            throw new SchedulingException("Failed to unschedule task: " + taskId, e);
        }
    }
}
