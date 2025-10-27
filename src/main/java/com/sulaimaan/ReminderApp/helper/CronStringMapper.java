package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.RecurrencePattern;
import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CronStringMapper {

    private static final Logger logger = LoggerFactory.getLogger(CronStringMapper.class);

    public String buildCronExpression(TimeDetail timeDetail, RecurrencePattern recurrencePattern, RecurrenceType recurrenceType) {
        logger.info("CronStringMapper.buildCronExpression | type={} tz={}", recurrenceType, timeDetail.timezone);

        TimeConverter converter = new TimeConverter();
        TimeConverter.UtcTime utcTime = converter.convertToUtc(timeDetail);
        String timePart = mapTime(utcTime);
        String datePart = mapDatePattern(recurrenceType, recurrencePattern);
        String cron = timePart + " " + datePart;

        logger.info("CronStringMapper.buildCronExpression | cron={}", cron);
        return cron;
    }

    private String mapTime(TimeConverter.UtcTime utcTime) {
        return utcTime.seconds + " " + utcTime.minutes + " " + utcTime.hours;
    }

    private String mapDatePattern(RecurrenceType recurrenceType, RecurrencePattern pattern) {
        switch (recurrenceType) {
            case SIMPLE:
                return pattern.dayOfMonth + " " + pattern.month + " " + pattern.dayOfWeek + " " + pattern.year;
            case DAILY:
                return "* * ? *";
            case WEEKLY:
                return "? * " + pattern.dayOfWeek + " *";
            case MONTHLY:
                return pattern.dayOfMonth + " * ? *";
            case YEARLY:
                return pattern.dayOfMonth + " " + pattern.month + " ? *";
            default:
                return "* * ? *";
        }
    }
}
