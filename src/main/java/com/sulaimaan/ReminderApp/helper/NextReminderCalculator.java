package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.RecurrencePattern;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for calculating the next valid reminder time based on recurrence rules
 */
public class NextReminderCalculator {

    private static final Logger logger = LoggerFactory.getLogger(NextReminderCalculator.class);

    private static final Map<String, DayOfWeek> DAY_OF_WEEK_MAP = Map.of(
            "MON", DayOfWeek.MONDAY,
            "TUE", DayOfWeek.TUESDAY,
            "WED", DayOfWeek.WEDNESDAY,
            "THU", DayOfWeek.THURSDAY,
            "FRI", DayOfWeek.FRIDAY,
            "SAT", DayOfWeek.SATURDAY,
            "SUN", DayOfWeek.SUNDAY
    );

    /**
     * Calculates next reminder time from current time with pattern and type
     */
    public ZonedDateTime calculateNext(ZonedDateTime clientTime, RecurrencePattern pattern, RecurrenceType type) {
        logger.info("Calculating next reminder | Type: {}, ClientTime: {}, Pattern: D={}, W={}, M={}, Y={}",
                type, clientTime, pattern.dayOfMonth, pattern.dayOfWeek, pattern.month, pattern.year);

        ZonedDateTime now = ZonedDateTime.now(clientTime.getZone());
        ZonedDateTime candidate = clientTime;

        switch (type) {
            case SIMPLE:
                if (candidate.isBefore(now)) {
                    logger.error("SIMPLE task time is in the past: {}", candidate);
                    throw new InvalidInputException("Simple reminder time must be in the future.");
                }
                logger.info("SIMPLE task scheduled at: {}", candidate);
                return candidate;

            case DAILY:
                if (candidate.isBefore(now)) {
                    candidate = candidate.plusDays(1);
                    logger.debug("DAILY: Time passed today, moved to tomorrow: {}", candidate);
                }
                logger.info("DAILY task next reminder: {}", candidate);
                return candidate;

            case WEEKLY:
                Set<DayOfWeek> allowedDays = parseDayOfWeek(pattern.dayOfWeek);
                logger.debug("WEEKLY allowed days: {}", allowedDays);

                if (candidate.isBefore(now)) {
                    candidate = candidate.plusDays(1);
                }

                int daysChecked = 0;
                while (!allowedDays.contains(candidate.getDayOfWeek()) && daysChecked < 7) {
                    candidate = candidate.plusDays(1);
                    daysChecked++;
                }

                if (daysChecked >= 7) {
                    logger.error("WEEKLY: No valid day found in pattern: {}", pattern.dayOfWeek);
                    throw new InvalidInputException("Invalid weekly pattern: " + pattern.dayOfWeek);
                }

                logger.info("WEEKLY task next reminder: {} ({})", candidate, candidate.getDayOfWeek());
                return candidate;

            case MONTHLY:
                Set<Integer> allowedDaysOfMonth = parseDayOfMonth(pattern.dayOfMonth);
                logger.debug("MONTHLY allowed days: {}", allowedDaysOfMonth);

                if (candidate.isBefore(now)) {
                    candidate = candidate.plusDays(1);
                }

                int monthsChecked = 0;
                while (monthsChecked < 12) {
                    if (allowedDaysOfMonth.contains(candidate.getDayOfMonth())) {
                        logger.info("MONTHLY task next reminder: {}", candidate);
                        return candidate;
                    }
                    candidate = candidate.plusDays(1);
                    if (candidate.getDayOfMonth() == 1) {
                        monthsChecked++;
                    }
                }

                logger.error("MONTHLY: No valid day found in pattern: {}", pattern.dayOfMonth);
                throw new InvalidInputException("Invalid monthly pattern: " + pattern.dayOfMonth);

            case YEARLY:
                if (candidate.isBefore(now)) {
                    candidate = candidate.plusYears(1);
                    logger.debug("YEARLY: Time passed this year, moved to next year: {}", candidate);
                }
                logger.info("YEARLY task next reminder: {}", candidate);
                return candidate;

            default:
                logger.error("Unsupported RecurrenceType: {}", type);
                throw new InvalidInputException("Unsupported recurrence type: " + type);
        }
    }

    /**
     * Overloaded method: Calculates next reminder from current time using stored cron expression
     */
    public ZonedDateTime calculateNext(ZonedDateTime currentReminderAt, String cronExpression, RecurrenceType type) {
        logger.info("Calculating next reminder from cron | Cron: {}, Type: {}, Current: {}",
                cronExpression, type, currentReminderAt);

        RecurrencePattern pattern = parseCronToPattern(cronExpression);
        logger.debug("Parsed cron to pattern: D={}, W={}, M={}, Y={}",
                pattern.dayOfMonth, pattern.dayOfWeek, pattern.month, pattern.year);

        return calculateNext(currentReminderAt, pattern, type);
    }

    /**
     * Parses cron expression back to RecurrencePattern
     */
    private RecurrencePattern parseCronToPattern(String cronExpression) {
        String[] parts = cronExpression.trim().split("\\s+");

        if (parts.length < 6) {
            logger.error("Invalid cron expression format: {}", cronExpression);
            throw new InvalidInputException("Invalid cron expression: " + cronExpression);
        }

        RecurrencePattern pattern = new RecurrencePattern();
        pattern.dayOfMonth = parts[3];
        pattern.month = parts[4];
        pattern.dayOfWeek = parts[5];
        pattern.year = (parts.length > 6) ? parts[6] : "*";

        return pattern;
    }

    /**
     * Parses dayOfWeek pattern string to Set of DayOfWeek (expects short names: MON, TUE, WED, etc.)
     */
    private Set<DayOfWeek> parseDayOfWeek(String pattern) {
        Set<DayOfWeek> days = new HashSet<>();

        if (pattern.equals("?") || pattern.equals("*")) {
            return Set.of(DayOfWeek.values());
        }

        String[] parts = pattern.split(",");
        for (String part : parts) {
            part = part.trim().toUpperCase();

            if (part.contains("-")) {
                String[] range = part.split("-");
                DayOfWeek start = DAY_OF_WEEK_MAP.get(range[0].trim());
                DayOfWeek end = DAY_OF_WEEK_MAP.get(range[1].trim());

                if (start == null || end == null) {
                    throw new InvalidInputException("Invalid day of week range: " + part);
                }

                DayOfWeek current = start;
                while (true) {
                    days.add(current);
                    if (current == end) break;
                    current = current.plus(1);
                }
            } else {
                DayOfWeek day = DAY_OF_WEEK_MAP.get(part);
                if (day == null) {
                    throw new InvalidInputException("Invalid day of week: " + part);
                }
                days.add(day);
            }
        }

        return days;
    }

    /**
     * Parses dayOfMonth pattern string to Set of integers
     */
    private Set<Integer> parseDayOfMonth(String pattern) {
        Set<Integer> days = new HashSet<>();

        if (pattern.equals("?") || pattern.equals("*")) {
            for (int i = 1; i <= 31; i++) {
                days.add(i);
            }
            return days;
        }

        String[] parts = pattern.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.contains("-")) {
                String[] range = part.split("-");
                int start = Integer.parseInt(range[0].trim());
                int end = Integer.parseInt(range[1].trim());
                for (int i = start; i <= end; i++) {
                    days.add(i);
                }
            } else {
                days.add(Integer.parseInt(part));
            }
        }

        return days;
    }
}
