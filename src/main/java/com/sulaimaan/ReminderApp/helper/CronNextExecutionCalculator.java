package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CronNextExecutionCalculator {

    private static final Logger logger = LoggerFactory.getLogger(CronNextExecutionCalculator.class);

    public ZonedDateTime getNextExecutionTime(String cronExpression) {
        return getNextExecutionTime(cronExpression, ZonedDateTime.now(ZoneOffset.UTC));
    }

    public ZonedDateTime getNextExecutionTime(String cronExpression, ZonedDateTime fromTime) {
        logger.info("CronNextExecutionCalculator | cron='{}' from={}", cronExpression, fromTime);
        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            ZonedDateTime next = cron.next(fromTime);
            if (next == null) {
                logger.warn("CronNextExecutionCalculator | no future execution for cron={}", cronExpression);
                throw new InvalidInputException("No future execution time for cron expression: " + cronExpression);
            }
            logger.info("CronNextExecutionCalculator | next={}", next);
            return next;
        } catch (IllegalArgumentException e) {
            logger.error("CronNextExecutionCalculator | invalid cron={} error={}", cronExpression, e.getMessage());
            throw new InvalidInputException("Invalid cron expression: " + cronExpression + " - " + e.getMessage());
        }
    }
}
