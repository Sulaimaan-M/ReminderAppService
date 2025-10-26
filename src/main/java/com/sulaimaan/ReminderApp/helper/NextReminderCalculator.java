package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.RecurrencePattern;
import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class NextReminderCalculator {

    public ZonedDateTime calculateNextReminder(TimeDetail timeDetail, RecurrencePattern pattern, RecurrenceType recurrenceType) {
        ZoneId clientZone;
        try {
            clientZone = ZoneId.of(timeDetail.timezone);
        } catch (Exception e) {
            throw new InvalidInputException("Invalid timezone: " + timeDetail.timezone);
        }

        ZonedDateTime nowInClientZone = ZonedDateTime.now(clientZone);

        switch (recurrenceType) {
            case SIMPLE -> {
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

                return exactDateTimeInClientZone;
            }

            case DAILY -> {
                ZonedDateTime todayAtTimeInClientZone = nowInClientZone
                        .withHour(timeDetail.hours)
                        .withMinute(timeDetail.minutes)
                        .withSecond(timeDetail.seconds)
                        .withNano(0);

                if (todayAtTimeInClientZone.isBefore(nowInClientZone) || todayAtTimeInClientZone.isEqual(nowInClientZone)) {
                    return todayAtTimeInClientZone.plusDays(1);
                }

                return todayAtTimeInClientZone;
            }

            case WEEKLY -> {
                int targetDayOfWeek = parseDayOfWeek(pattern.dayOfWeek);
                int currentDayOfWeek = nowInClientZone.getDayOfWeek().getValue();

                ZonedDateTime thisWeekTargetInClientZone = nowInClientZone
                        .plusDays(targetDayOfWeek - currentDayOfWeek)
                        .withHour(timeDetail.hours)
                        .withMinute(timeDetail.minutes)
                        .withSecond(timeDetail.seconds)
                        .withNano(0);

                if (thisWeekTargetInClientZone.isBefore(nowInClientZone) || thisWeekTargetInClientZone.isEqual(nowInClientZone)) {
                    return thisWeekTargetInClientZone.plusWeeks(1);
                }

                return thisWeekTargetInClientZone;
            }

            case MONTHLY -> {
                int targetDay = Integer.parseInt(pattern.dayOfMonth);

                ZonedDateTime thisMonthTargetInClientZone = nowInClientZone
                        .withDayOfMonth(targetDay)
                        .withHour(timeDetail.hours)
                        .withMinute(timeDetail.minutes)
                        .withSecond(timeDetail.seconds)
                        .withNano(0);

                if (thisMonthTargetInClientZone.isBefore(nowInClientZone) || thisMonthTargetInClientZone.isEqual(nowInClientZone)) {
                    return thisMonthTargetInClientZone.plusMonths(1);
                }

                return thisMonthTargetInClientZone;
            }

            case YEARLY -> {
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
                    return thisYearTargetInClientZone.plusYears(1);
                }

                return thisYearTargetInClientZone;
            }

            default -> throw new InvalidInputException("Invalid recurrence type: " + recurrenceType);
        }
    }

    private int parseDayOfWeek(String dayOfWeek) {
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
