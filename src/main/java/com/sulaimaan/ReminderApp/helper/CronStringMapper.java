package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.RecurrencePattern;
import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;

public class CronStringMapper {

    public String buildCronExpression(TimeDetail timeDetail, RecurrencePattern recurrencePattern, RecurrenceType recurrenceType) {
        String timePart = mapTime(timeDetail);
        String datePart = mapDatePattern(recurrenceType, recurrencePattern);

        return timePart + " " + datePart;
    }

    private String mapTime(TimeDetail time) {
        return time.seconds + " " + time.minutes + " " + time.hours;
    }

    private String mapDatePattern(RecurrenceType recurrenceType, RecurrencePattern pattern) {
        String cronExpression = "";

        switch (recurrenceType) {
            case SIMPLE -> {
                // Use exact values from pattern
                cronExpression = pattern.dayOfMonth + " " + pattern.month + " " + pattern.dayOfWeek + " " + pattern.year;
            }
            case DAILY -> {
                // Every day at specified time
                cronExpression = "* * ? *";
            }
            case WEEKLY -> {
                // Specific days of week
                cronExpression = "? * " + pattern.dayOfWeek + " *";
            }
            case MONTHLY -> {
                // Specific day of month
                cronExpression = pattern.dayOfMonth + " * ? *";
            }
            case YEARLY -> {
                // Specific month and day
                cronExpression = pattern.dayOfMonth + " " + pattern.month + " ? *";
            }
        }

        return cronExpression;
    }
}
