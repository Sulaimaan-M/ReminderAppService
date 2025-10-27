package com.sulaimaan.ReminderApp.quartz;

import com.sulaimaan.ReminderApp.config.ApplicationContextProvider;
import com.sulaimaan.ReminderApp.entity.Reminder;
import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.helper.CronNextExecutionCalculator;
import com.sulaimaan.ReminderApp.helper.RecurrenceType;
import com.sulaimaan.ReminderApp.repository.ReminderRepository;
import com.sulaimaan.ReminderApp.repository.TaskRepository;
import com.sulaimaan.ReminderApp.service.SchedulingService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ReminderNotificationJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ReminderNotificationJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long taskId = context.getJobDetail().getJobDataMap().getLong("taskId");
        String taskText = context.getJobDetail().getJobDataMap().getString("taskText");
        String fcmToken = context.getJobDetail().getJobDataMap().getString("fcmToken");

        logger.info("🔔 Job fire | taskId={} text='{}' tokenLen={}", taskId, taskText, fcmToken == null ? 0 : fcmToken.length());

        TaskRepository taskRepository = ApplicationContextProvider.getBean(TaskRepository.class);
        ReminderRepository reminderRepository = ApplicationContextProvider.getBean(ReminderRepository.class);
        SchedulingService schedulingService = ApplicationContextProvider.getBean(SchedulingService.class);
        CronNextExecutionCalculator cronCalc = ApplicationContextProvider.getBean(CronNextExecutionCalculator.class);

        // Load the task
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            logger.warn("Job | task not found id={}", taskId);
            return;
        }

        // 1) Persist a new Reminder instance for this fire
        ZonedDateTime remindedAt = task.getNextReminderAt(); // per requirement
        if (remindedAt == null) {
            // Fallback: if somehow missing, use now to avoid NPEs
            remindedAt = ZonedDateTime.now(ZoneOffset.UTC);
            logger.warn("Job | task.nextReminderAt was null, using now={}", remindedAt);
        }

        Reminder reminder = new Reminder();
        reminder.setTask(task);
        reminder.setRemindedAt(remindedAt);
        // isCompleted defaults to false in entity
        reminder = reminderRepository.save(reminder);

        logger.info("Job | reminder saved id={} taskId={} remindedAt={} completed={}",
                reminder.getId(), task.getId(), reminder.getRemindedAt(), reminder.getCompleted());

        // 2) Update task.nextReminderAt to next occurrence and reschedule if recurring
        if (task.getRecurrenceType() == RecurrenceType.SIMPLE) {
            // One-time reminder: unschedule and do not compute next
            logger.info("Job | SIMPLE task -> unscheduling id={}", task.getId());
            schedulingService.unscheduleTask(task.getId());
            return;
        }

        // Next execution time using cron (UTC)
        try {
            ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
            ZonedDateTime nextUtc = cronCalc.getNextExecutionTime(task.getCronExpression(), nowUtc);
            // Store as ZonedDateTime (UTC). If you prefer client zone here, you’d need to store timezone in Task.
            task.setNextReminderAt(nextUtc);
            taskRepository.save(task);
            logger.info("Job | task.nextReminderAt updated id={} nextAt={}", task.getId(), nextUtc);

            // Reschedule job to pick up the new nextReminderAt
            schedulingService.rescheduleTask(task);
            logger.info("Job | rescheduled id={}", task.getId());
        } catch (Exception e) {
            logger.error("Job | failed to compute/reschedule next for taskId={} error={}", task.getId(), e.getMessage(), e);
            // We don't rethrow to avoid job misfire storm; log for investigation.
        }

        // TODO: Send FCM notification with fcmToken (when you wire NotificationService)
        logger.info("🔔 Job done | taskId={}", taskId);
    }
}
