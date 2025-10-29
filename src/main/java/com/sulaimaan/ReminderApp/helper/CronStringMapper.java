package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.RecurrencePattern;
import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;

/**
 * Utility class for building Quartz cron expressions from time details and recurrence patterns
 */
public class CronStringMapper {

    private static final Logger logger = LoggerFactory.getLogger(CronStringMapper.class);

    /**
     * Builds a UTC-based cron expression from client time details and recurrence pattern
     */
    public String buildCronExpression(TimeDetail timeDetail, RecurrencePattern pattern, RecurrenceType type) {
        logger.info("🛠️ Building Cron | Type: {}, Time(client): {}:{}:{} @ {}, Pattern: D={}, W={}, M={}, Y={}",
                type, timeDetail.hours, timeDetail.minutes, timeDetail.seconds, timeDetail.timezone,
                pattern.dayOfMonth, pattern.dayOfWeek, pattern.month, pattern.year);

        LocalTime utcTime = TimeConverter.convertToUtc(timeDetail);
        int utcSecond = utcTime.getSecond();
        int utcMinute = utcTime.getMinute();
        int utcHour = utcTime.getHour();

        logger.debug(" UTC Time Components for Cron: {}:{}:{}", utcHour, utcMinute, utcSecond);

        String cronDayOfMonth = pattern.dayOfMonth;
        String cronMonth = pattern.month;
        String cronDayOfWeek = pattern.dayOfWeek;
        String cronYear = pattern.year;

        switch (type) {
            case SIMPLE:
                logger.debug(" SIMPLE | Using specific date from pattern for cron structure.");
                break;
            case DAILY:
                cronDayOfMonth = "*";
                cronMonth = "*";
                cronDayOfWeek = "?";
                cronYear = "*";
                logger.debug(" DAILY | Setting D='*', M='*', W='?', Y='*'");
                break;
            case WEEKLY:
                cronDayOfMonth = "?";
                cronMonth = "*";
                logger.debug(" WEEKLY | Using W='{}' from pattern, setting D='?', M='*', Y='*'", cronDayOfWeek);
                cronYear = "*";
                break;
            case MONTHLY:
                cronMonth = "*";
                cronDayOfWeek = "?";
                cronYear = "*";
                logger.debug(" MONTHLY | Using D='{}' from pattern, setting W='?', M='*', Y='*'", cronDayOfMonth);
                break;
            case YEARLY:
                cronDayOfWeek = "?";
                cronYear = "*";
                logger.debug(" YEARLY | Using D='{}', M='{}' from pattern, setting W='?', Y='*'", cronDayOfMonth, cronMonth);
                break;
            default:
                logger.error("❌ Unsupported RecurrenceType for Cron: {}", type);
                throw new IllegalArgumentException("Unsupported recurrence type: " + type);
        }

        String cronExpression = String.format("%d %d %d %s %s %s",
                utcSecond, utcMinute, utcHour, cronDayOfMonth, cronMonth, cronDayOfWeek);

        logger.info("✅ Cron Expression (UTC based): {}", cronExpression);
        return cronExpression;
    }
}
