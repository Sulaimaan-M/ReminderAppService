package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility component for calculating the next execution time based on a Quartz cron expression
 */
@Component
public class CronNextExecutionCalculator {

    private static final Logger logger = LoggerFactory.getLogger(CronNextExecutionCalculator.class);

    /**
     * Calculates the next execution time from the current UTC time
     */
    public ZonedDateTime getNextExecutionTime(String cronExpression) {
        return getNextExecutionTime(cronExpression, ZonedDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Calculates the next execution time from a specified point in time using Quartz cron expression
     */
    public ZonedDateTime getNextExecutionTime(String cronExpression, ZonedDateTime fromTime) {
        logger.info("CronNextExecutionCalculator | (Quartz) cron='{}' from={}", cronExpression, fromTime);
        try {
            CronExpression quartzCron = new CronExpression(cronExpression);
            quartzCron.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date fromDate = Date.from(fromTime.toInstant());
            Date next = quartzCron.getNextValidTimeAfter(fromDate);
            if (next == null) {
                logger.warn("CronNextExecutionCalculator | no future execution for cron={}", cronExpression);
                throw new InvalidInputException("No future execution time for cron expression: " + cronExpression);
            }

            ZonedDateTime nextZdt = ZonedDateTime.ofInstant(next.toInstant(), ZoneOffset.UTC);
            logger.info("CronNextExecutionCalculator | next={}", nextZdt);
            return nextZdt;

        } catch (ParseException e) {
            logger.error("CronNextExecutionCalculator | invalid cron={} error={}", cronExpression, e.getMessage());
            throw new InvalidInputException("Invalid cron expression: " + cronExpression + " - " + e.getMessage());
        } catch (Exception e) {
            logger.error("CronNextExecutionCalculator | unexpected error cron={} error={}", cronExpression, e.getMessage(), e);
            throw new InvalidInputException("Failed to compute next execution for cron: " + cronExpression);
        }
    }
}
