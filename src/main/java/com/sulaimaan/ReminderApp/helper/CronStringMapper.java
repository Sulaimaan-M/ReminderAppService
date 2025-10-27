package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.RecurrencePattern;
import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;

public class CronStringMapper {

    private static final Logger logger = LoggerFactory.getLogger(CronStringMapper.class); // 🪵 Logger instance

    // Mapping: second minute hour dayOfMonth month dayOfWeek year (optional)
    public String buildCronExpression(TimeDetail timeDetail, RecurrencePattern pattern, RecurrenceType type) {
        // 🪵 Log the inputs for cron string generation
        logger.info("🛠️ Building Cron | Type: {}, Time(client): {}:{}:{} @ {}, Pattern: D={}, W={}, M={}, Y={}",
                type, timeDetail.hours, timeDetail.minutes, timeDetail.seconds, timeDetail.timezone, // Use field access
                pattern.dayOfMonth, pattern.dayOfWeek, pattern.month, pattern.year); // Use field access

        // Convert client time details to UTC time components
        LocalTime utcTime = TimeConverter.convertToUtc(timeDetail);
        int utcSecond = utcTime.getSecond();
        int utcMinute = utcTime.getMinute();
        int utcHour = utcTime.getHour();

        // 🪵 Log the UTC time components being used
        logger.debug(" UTC Time Components for Cron: {}:{}:{}", utcHour, utcMinute, utcSecond);

        String cronDayOfMonth = pattern.dayOfMonth; // Use field access
        String cronMonth = pattern.month; // Use field access
        String cronDayOfWeek = pattern.dayOfWeek; // Use field access
        String cronYear = pattern.year; // Use field access

        // Adjust pattern based on recurrence type, using UTC time
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

        // 🪵 Log the final generated cron expression
        logger.info("✅ Cron Expression (UTC based): {}", cronExpression);
        return cronExpression;
    }
}
