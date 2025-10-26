package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import org.springframework.scheduling.support.CronExpression;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CronNextExecutionCalculator {

    public ZonedDateTime getNextExecutionTime(String cronExpression) {
        return getNextExecutionTime(cronExpression, ZonedDateTime.now(ZoneOffset.UTC));
    }

    public ZonedDateTime getNextExecutionTime(String cronExpression, ZonedDateTime fromTime) {
        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            ZonedDateTime nextExecution = cron.next(fromTime);

            if (nextExecution == null) {
                throw new InvalidInputException("No future execution time for cron expression: " + cronExpression);
            }

            return nextExecution;

        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid cron expression: " + cronExpression + " - " + e.getMessage());
        }
    }

    public ZonedDateTime[] getNextExecutionTimes(String cronExpression, int count) {
        if (count <= 0) {
            throw new InvalidInputException("Count must be positive");
        }

        ZonedDateTime[] executions = new ZonedDateTime[count];
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneOffset.UTC);

        try {
            CronExpression cron = CronExpression.parse(cronExpression);

            for (int i = 0; i < count; i++) {
                ZonedDateTime nextExecution = cron.next(currentTime);

                if (nextExecution == null) {
                    throw new InvalidInputException("Could not calculate " + count + " future executions");
                }

                executions[i] = nextExecution;
                currentTime = nextExecution;
            }

            return executions;

        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid cron expression: " + cronExpression + " - " + e.getMessage());
        }
    }
}
