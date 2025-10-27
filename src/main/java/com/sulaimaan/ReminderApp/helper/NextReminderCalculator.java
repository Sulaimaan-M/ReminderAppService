package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.RecurrencePattern;
import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class NextReminderCalculator {

    private static final Logger logger = LoggerFactory.getLogger(NextReminderCalculator.class);

    public ZonedDateTime calculateNextReminder(TimeDetail timeDetail, RecurrencePattern pattern, RecurrenceType recurrenceType) {
        ZoneId clientZone;
        try {
            clientZone = ZoneId.of(timeDetail.timezone);
        } catch (Exception e) {
            logger.warn("NextReminderCalculator | invalid timezone={} fallback=UTC", timeDetail.timezone);
            throw new InvalidInputException("Invalid timezone: " + timeDetail.timezone);
        }

        ZonedDateTime now = ZonedDateTime.now(clientZone);
        logger.info("NextReminderCalculator | type={} now={} tz={}", recurrenceType, now, clientZone);

        switch (recurrenceType) {
            case SIMPLE: {
                ZonedDateTime exact = ZonedDateTime.now(clientZone)
                        .withYear(Integer.parseInt(pattern.year))
                        .withMonth(Integer.parseInt(pattern.month))
                        .withDayOfMonth(Integer.parseInt(pattern.dayOfMonth))
                        .withHour(timeDetail.hours)
                        .withMinute(timeDetail.minutes)
                        .withSecond(timeDetail.seconds)
                        .withNano(0);
                if (exact.isBefore(now)) {
                    logger.warn("NextReminderCalculator | SIMPLE past exact={}", exact);
                    throw new InvalidInputException("The specified reminder time is in the past");
                }
                logger.info("NextReminderCalculator | SIMPLE next={}", exact);
                return exact;
            }
            case DAILY: {
                ZonedDateTime today = now.withHour(timeDetail.hours).withMinute(timeDetail.minutes).withSecond(timeDetail.seconds).withNano(0);
                ZonedDateTime next = (today.isBefore(now) || today.isEqual(now)) ? today.plusDays(1) : today;
                logger.info("NextReminderCalculator | DAILY next={}", next);
                return next;
            }
            case WEEKLY: {
                int targetDow = parseDayOfWeek(pattern.dayOfWeek);
                int currentDow = now.getDayOfWeek().getValue();
                ZonedDateTime target = now.plusDays(targetDow - currentDow)
                        .withHour(timeDetail.hours).withMinute(timeDetail.minutes).withSecond(timeDetail.seconds).withNano(0);
                if (!target.isAfter(now)) target = target.plusWeeks(1);
                logger.info("NextReminderCalculator | WEEKLY next={}", target);
                return target;
            }
            case MONTHLY: {
                int targetDay = Integer.parseInt(pattern.dayOfMonth);
                ZonedDateTime target = now.withDayOfMonth(Math.min(targetDay, now.toLocalDate().lengthOfMonth()))
                        .withHour(timeDetail.hours).withMinute(timeDetail.minutes).withSecond(timeDetail.seconds).withNano(0);
                if (!target.isAfter(now)) target = target.plusMonths(1).withDayOfMonth(Math.min(targetDay, target.toLocalDate().lengthOfMonth()));
                logger.info("NextReminderCalculator | MONTHLY next={}", target);
                return target;
            }
            case YEARLY: {
                int targetMonth = Integer.parseInt(pattern.month);
                int targetDay = Integer.parseInt(pattern.dayOfMonth);
                ZonedDateTime target = now.withMonth(targetMonth)
                        .withDayOfMonth(Math.min(targetDay, now.withMonth(targetMonth).toLocalDate().lengthOfMonth()))
                        .withHour(timeDetail.hours).withMinute(timeDetail.minutes).withSecond(timeDetail.seconds).withNano(0);
                if (!target.isAfter(now)) {
                    ZonedDateTime nextYear = now.plusYears(1).withMonth(targetMonth);
                    target = nextYear.withDayOfMonth(Math.min(targetDay, nextYear.toLocalDate().lengthOfMonth()))
                            .withHour(timeDetail.hours).withMinute(timeDetail.minutes).withSecond(timeDetail.seconds).withNano(0);
                }
                logger.info("NextReminderCalculator | YEARLY next={}", target);
                return target;
            }
            default:
                logger.warn("NextReminderCalculator | invalid type={}", recurrenceType);
                throw new InvalidInputException("Invalid recurrence type: " + recurrenceType);
        }
    }

    private int parseDayOfWeek(String dayOfWeek) {
        switch (dayOfWeek.toUpperCase()) {
            case "MON":
            case "MONDAY":
            case "1":
                return 1;
            case "TUE":
            case "TUESDAY":
            case "2":
                return 2;
            case "WED":
            case "WEDNESDAY":
            case "3":
                return 3;
            case "THU":
            case "THURSDAY":
            case "4":
                return 4;
            case "FRI":
            case "FRIDAY":
            case "5":
                return 5;
            case "SAT":
            case "SATURDAY":
            case "6":
                return 6;
            case "SUN":
            case "SUNDAY":
            case "7":
                return 7;
            default:
                throw new InvalidInputException("Invalid day of week: " + dayOfWeek);
        }
    }
}
