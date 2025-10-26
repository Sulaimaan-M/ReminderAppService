package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.RecurrencePattern;
import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class NextReminderCalculator {

    public ZonedDateTime calculateNextReminder(TimeDetail timeDetail, RecurrencePattern pattern, RecurrenceType recurrenceType) {
        // Parse client timezone
        ZoneId clientZone;
        try {
            clientZone = ZoneId.of(timeDetail.timezone);
        } catch (Exception e) {
            throw new InvalidInputException("Invalid timezone: " + timeDetail.timezone);
        }

        // Get current time in client's timezone
        ZonedDateTime nowInClientZone = ZonedDateTime.now(clientZone);

        switch (recurrenceType) {
            case SIMPLE -> {
                // For SIMPLE: User provides complete date-time in their timezone
                ZonedDateTime exactDateTimeInClientZone = ZonedDateTime.now(clientZone)
                        .withYear(Integer.parseInt(pattern.year))
                        .withMonth(Integer.parseInt(pattern.month))
                        .withDayOfMonth(Integer.parseInt(pattern.dayOfMonth))
                        .withHour(timeDetail.hours)
                        .withMinute(timeDetail.minutes)
                        .withSecond(timeDetail.seconds)
                        .withNano(0);

                if (exactDateTimeInClientZone.isBefore(nowInClientZone)) {
                    throw new InvalidInputException("The specified reminder time is in the past");
                }

                // Convert to UTC before returning
                return exactDateTimeInClientZone.withZoneSameInstant(ZoneOffset.UTC);
            }

            case DAILY -> {
                // For DAILY: If time passed today in client timezone, schedule for tomorrow
                ZonedDateTime todayAtTimeInClientZone = nowInClientZone
                        .withHour(timeDetail.hours)
                        .withMinute(timeDetail.minutes)
                        .withSecond(timeDetail.seconds)
                        .withNano(0);

                if (todayAtTimeInClientZone.isBefore(nowInClientZone) || todayAtTimeInClientZone.isEqual(nowInClientZone)) {
                    todayAtTimeInClientZone = todayAtTimeInClientZone.plusDays(1);
                }

                // Convert to UTC before returning
                return todayAtTimeInClientZone.withZoneSameInstant(ZoneOffset.UTC);
            }

            case WEEKLY -> {
                // For WEEKLY: If this week's day passed, schedule for next week
                int targetDayOfWeek = parseDayOfWeek(pattern.dayOfWeek);
                int currentDayOfWeek = nowInClientZone.getDayOfWeek().getValue();

                ZonedDateTime thisWeekTargetInClientZone = nowInClientZone
                        .plusDays(targetDayOfWeek - currentDayOfWeek)
                        .withHour(timeDetail.hours)
                        .withMinute(timeDetail.minutes)
                        .withSecond(timeDetail.seconds)
                        .withNano(0);

                if (thisWeekTargetInClientZone.isBefore(nowInClientZone) || thisWeekTargetInClientZone.isEqual(nowInClientZone)) {
                    thisWeekTargetInClientZone = thisWeekTargetInClientZone.plusWeeks(1);
                }

                // Convert to UTC before returning
                return thisWeekTargetInClientZone.withZoneSameInstant(ZoneOffset.UTC);
            }

            case MONTHLY -> {
                // For MONTHLY: If this month's day passed, schedule for next month
                int targetDay = Integer.parseInt(pattern.dayOfMonth);

                ZonedDateTime thisMonthTargetInClientZone = nowInClientZone
                        .withDayOfMonth(targetDay)
                        .withHour(timeDetail.hours)
                        .withMinute(timeDetail.minutes)
                        .withSecond(timeDetail.seconds)
                        .withNano(0);

                if (thisMonthTargetInClientZone.isBefore(nowInClientZone) || thisMonthTargetInClientZone.isEqual(nowInClientZone)) {
                    thisMonthTargetInClientZone = thisMonthTargetInClientZone.plusMonths(1);
                }

                // Convert to UTC before returning
                return thisMonthTargetInClientZone.withZoneSameInstant(ZoneOffset.UTC);
            }

            case YEARLY -> {
                // For YEARLY: If this year's date passed, schedule for next year
                int targetMonth = Integer.parseInt(pattern.month);
                int targetDay = Integer.parseInt(pattern.dayOfMonth);

                ZonedDateTime thisYearTargetInClientZone = nowInClientZone
                        .withMonth(targetMonth)
                        .withDayOfMonth(targetDay)
                        .withHour(timeDetail.hours)
                        .withMinute(timeDetail.minutes)
                        .withSecond(timeDetail.seconds)
                        .withNano(0);

                if (thisYearTargetInClientZone.isBefore(nowInClientZone) || thisYearTargetInClientZone.isEqual(nowInClientZone)) {
                    thisYearTargetInClientZone = thisYearTargetInClientZone.plusYears(1);
                }

                // Convert to UTC before returning
                return thisYearTargetInClientZone.withZoneSameInstant(ZoneOffset.UTC);
            }

            default -> throw new InvalidInputException("Invalid recurrence type: " + recurrenceType);
        }
    }

    private int parseDayOfWeek(String dayOfWeek) {
        // Convert day names to numbers (1=Monday, 7=Sunday)
        return switch (dayOfWeek.toUpperCase()) {
            case "MON", "MONDAY", "1" -> 1;
            case "TUE", "TUESDAY", "2" -> 2;
            case "WED", "WEDNESDAY", "3" -> 3;
            case "THU", "THURSDAY", "4" -> 4;
            case "FRI", "FRIDAY", "5" -> 5;
            case "SAT", "SATURDAY", "6" -> 6;
            case "SUN", "SUNDAY", "7" -> 7;
            default -> throw new InvalidInputException("Invalid day of week: " + dayOfWeek);
        };
    }
}
