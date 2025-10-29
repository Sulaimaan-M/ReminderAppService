package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.RecurrencePattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Utility class for building Quartz cron expressions from ZonedDateTime and recurrence patterns
 */
public class CronStringMapper {

    private static final Logger logger = LoggerFactory.getLogger(CronStringMapper.class);

    /**
     * Builds UTC-based cron expression from next reminder time and recurrence pattern
     */
    public String buildCronExpression(ZonedDateTime nextReminderAt, RecurrencePattern pattern, RecurrenceType type) {
        logger.info("Building cron expression | Type: {}, NextReminderAt: {}, Pattern: D={}, W={}, M={}, Y={}",
                type, nextReminderAt, pattern.dayOfMonth, pattern.dayOfWeek, pattern.month, pattern.year);

        ZonedDateTime utcTime = nextReminderAt.withZoneSameInstant(ZoneOffset.UTC);
        int utcSecond = utcTime.getSecond();
        int utcMinute = utcTime.getMinute();
        int utcHour = utcTime.getHour();

        logger.debug("Converted to UTC time components: {}:{}:{}", utcHour, utcMinute, utcSecond);

        String cronDayOfMonth = pattern.dayOfMonth;
        String cronMonth = pattern.month;
        String cronDayOfWeek = pattern.dayOfWeek;

        switch (type) {
            case SIMPLE:
                int dayOfMonth = utcTime.getDayOfMonth();
                int month = utcTime.getMonthValue();
                cronDayOfMonth = String.valueOf(dayOfMonth);
                cronMonth = String.valueOf(month);
                cronDayOfWeek = "?";
                logger.debug("SIMPLE: Using specific date D={}, M={}", cronDayOfMonth, cronMonth);
                break;

            case DAILY:
                cronDayOfMonth = "*";
                cronMonth = "*";
                cronDayOfWeek = "?";
                logger.debug("DAILY: D='*', M='*', W='?'");
                break;

            case WEEKLY:
                cronDayOfMonth = "?";
                cronMonth = "*";
                logger.debug("WEEKLY: Using pattern W='{}', D='?', M='*'", cronDayOfWeek);
                break;

            case MONTHLY:
                cronMonth = "*";
                cronDayOfWeek = "?";
                logger.debug("MONTHLY: Using pattern D='{}', W='?', M='*'", cronDayOfMonth);
                break;

            case YEARLY:
                cronDayOfWeek = "?";
                logger.debug("YEARLY: Using pattern D='{}', M='{}', W='?'", cronDayOfMonth, cronMonth);
                break;

            default:
                logger.error("Unsupported RecurrenceType: {}", type);
                throw new IllegalArgumentException("Unsupported recurrence type: " + type);
        }

        String cronExpression = String.format("%d %d %d %s %s %s",
                utcSecond, utcMinute, utcHour, cronDayOfMonth, cronMonth, cronDayOfWeek);

        logger.info("Built cron expression (UTC): {}", cronExpression);
        return cronExpression;
    }
}
