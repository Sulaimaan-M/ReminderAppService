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
        logger.info("SchedulingService.scheduleTask | id={} type={} nextAt={} cron={}",
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
                logger.info("SchedulingService.scheduleTask | SimpleTrigger at {}", fireTime);
            } else {
                CronScheduleBuilder cronSchedule = CronScheduleBuilder
                        .cronSchedule(task.getCronExpression())
                        .inTimeZone(TimeZone.getTimeZone("UTC")); // enforce UTC
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity("trigger-" + task.getId(), "reminder-triggers")
                        .withSchedule(cronSchedule)
                        .build();
                logger.info("SchedulingService.scheduleTask | CronTrigger UTC");
            }

            scheduler.scheduleJob(jobDetail, trigger);
            logger.info("SchedulingService.scheduleTask | scheduled id={}", task.getId());

        } catch (SchedulerException e) {
            logger.error("SchedulingService.scheduleTask | failed id={} error={}", task.getId(), e.getMessage(), e);
            throw new SchedulingException("Failed to schedule task: " + task.getId(), e);
        } catch (Exception e) {
            logger.error("SchedulingService.scheduleTask | unexpected id={} error={}", task.getId(), e.getMessage(), e);
            throw new SchedulingException("Unexpected error scheduling task: " + task.getId(), e);
        }
    }

    public void rescheduleTask(Task task) {
        logger.info("SchedulingService.rescheduleTask | id={}", task.getId());
        try {
            unscheduleTask(task.getId());
            scheduleTask(task);
            logger.info("SchedulingService.rescheduleTask | done id={}", task.getId());
        } catch (Exception e) {
            logger.error("SchedulingService.rescheduleTask | failed id={} error={}", task.getId(), e.getMessage(), e);
            throw new SchedulingException("Failed to reschedule task: " + task.getId(), e);
        }
    }

    public void unscheduleTask(Long taskId) {
        logger.info("SchedulingService.unscheduleTask | id={}", taskId);
        try {
            JobKey jobKey = new JobKey("task-" + taskId, "reminder-jobs");
            boolean deleted = scheduler.deleteJob(jobKey);
            logger.info("SchedulingService.unscheduleTask | id={} deleted={}", taskId, deleted);
        } catch (SchedulerException e) {
            logger.error("SchedulingService.unscheduleTask | failed id={} error={}", taskId, e.getMessage(), e);
            throw new SchedulingException("Failed to unschedule task: " + taskId, e);
        }
    }
}
