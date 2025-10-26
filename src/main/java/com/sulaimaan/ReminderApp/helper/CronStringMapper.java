package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.RecurrencePattern;
import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;

public class CronStringMapper {

    public String buildCronExpression(TimeDetail timeDetail, RecurrencePattern recurrencePattern, RecurrenceType recurrenceType) {
        TimeConverter converter = new TimeConverter();
        TimeConverter.UtcTime utcTime = converter.convertToUtc(timeDetail);

        String timePart = mapTime(utcTime);
        String datePart = mapDatePattern(recurrenceType, recurrencePattern);

        return timePart + " " + datePart;
    }

    private String mapTime(TimeConverter.UtcTime utcTime) {
        return utcTime.seconds + " " + utcTime.minutes + " " + utcTime.hours;
    }

    private String mapDatePattern(RecurrenceType recurrenceType, RecurrencePattern pattern) {
        String cronExpression = "";

        switch (recurrenceType) {
            case SIMPLE -> {
                cronExpression = pattern.dayOfMonth + " " + pattern.month + " " + pattern.dayOfWeek + " " + pattern.year;
            }
            case DAILY -> {
                cronExpression = "* * ? *";
            }
            case WEEKLY -> {
                cronExpression = "? * " + pattern.dayOfWeek + " *";
            }
            case MONTHLY -> {
                cronExpression = pattern.dayOfMonth + " * ? *";
            }
            case YEARLY -> {
                cronExpression = pattern.dayOfMonth + " " + pattern.month + " ? *";
            }
        }

        return cronExpression;
    }
}
