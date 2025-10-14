package com.sulaimaan.ReminderApp.quartz;

import com.sulaimaan.ReminderApp.entity.Reminder;
import com.sulaimaan.ReminderApp.helper.CronStringBuilder;
import com.sulaimaan.ReminderApp.helper.IntervalType;
import com.sulaimaan.ReminderApp.quartz.job.ReminderJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class QuartzReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(QuartzReminderScheduler.class);

    private final Scheduler scheduler;

    public QuartzReminderScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void schedule(Reminder reminder) {
        if (reminder == null || reminder.getId() == null) {
            throw new IllegalArgumentException("Reminder must be persisted (have an ID) before scheduling");
        }

        JobDetail jobDetail = JobBuilder.newJob(ReminderJob.class)
                .withIdentity(reminder.getId().toString())
                .usingJobData("message", reminder.getText())
                .build();

        Trigger trigger;
        if (reminder.getIntervalType() == IntervalType.SIMPLE) {
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(reminder.getId().toString())
                    .startAt(Date.from(reminder.getRemindAt().toInstant()))
                    .build();
        } else {
            String cronExpression = CronStringBuilder.build(reminder.getRemindAt(), reminder.getIntervalType());
            trigger = TriggerBuilder.newTrigger()
                    .forJob(jobDetail)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();
        }

        try {
            scheduler.scheduleJob(jobDetail, trigger);
            logger.debug("Scheduled reminder job {} with trigger type: {}", reminder.getId(), reminder.getIntervalType());
        } catch (SchedulerException e) {
            logger.error("Failed to schedule Quartz job for reminder ID: {}", reminder.getId(), e);
            throw new RuntimeException("Scheduling failed for reminder: " + reminder.getId(), e);
        }
    }
}
