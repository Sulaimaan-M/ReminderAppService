package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.RecurrencePattern;
import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;

public class NextReminderCalculator {

    private static final Logger logger = LoggerFactory.getLogger(NextReminderCalculator.class); // 🪵 Logger instance

    public ZonedDateTime calculateNextReminder(TimeDetail timeDetail, RecurrencePattern pattern, RecurrenceType type) {
        // 🪵 Log the inputs for calculation
        logger.info("🚀 Calculating Next Reminder | Type: {}, Time: {}:{}:{}, Zone: {}, Pattern: D={}, W={}, M={}, Y={}",
                type, timeDetail.hours, timeDetail.minutes, timeDetail.seconds, timeDetail.timezone, // Use field access
                pattern.dayOfMonth, pattern.dayOfWeek, pattern.month, pattern.year); // Use field access

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeDetail.timezone); // Use field access
            logger.debug("🌍 ZoneId for calculation: {}", zoneId);
        } catch (Exception e) {
            logger.error("❌ Invalid timezone string for calculation: {}", timeDetail.timezone, e); // Use field access
            throw new InvalidInputException("Invalid timezone: " + timeDetail.timezone); // Use field access
        }

        ZonedDateTime nowInClientZone = ZonedDateTime.now(zoneId);
        logger.debug("⏳ Current time in client zone [{}]: {}", zoneId, nowInClientZone);

        // Create the initial 'target' time based on today's date in the client's zone
        ZonedDateTime targetTime = nowInClientZone
                .withHour(timeDetail.hours) // Use field access
                .withMinute(timeDetail.minutes) // Use field access
                .withSecond(timeDetail.seconds) // Use field access
                .withNano(0); // Ensure nano is 0

        logger.debug("🎯 Initial target time based on 'now': {}", targetTime);

        ZonedDateTime nextReminder;

        switch (type) {
            case SIMPLE:
                int year = Integer.parseInt(pattern.year); // Use field access
                int month = Integer.parseInt(pattern.month); // Use field access
                int day = Integer.parseInt(pattern.dayOfMonth); // Use field access
                // For SIMPLE, use the exact date provided in the pattern
                ZonedDateTime exact = ZonedDateTime.of(LocalDate.of(year, month, day),
                        LocalTime.of(timeDetail.hours, timeDetail.minutes, timeDetail.seconds), // Use field access
                        zoneId);
                logger.debug(" SIMPLE | Exact specified time: {}", exact);
                // Simple tasks should ideally be in the future, throw if not.
                if (exact.isBefore(nowInClientZone)) {
                    logger.warn("❌ SIMPLE task specified time {} is in the past compared to {}", exact, nowInClientZone);
                    throw new InvalidInputException("Simple reminder time must be in the future.");
                }
                nextReminder = exact;
                break;

            case DAILY:
                nextReminder = adjustToFuture(targetTime, nowInClientZone, adjusted -> adjusted.plusDays(1));
                logger.debug(" DAILY | Adjusted target time: {}", nextReminder);
                break;

            case WEEKLY:
                nextReminder = adjustToFuture(targetTime, nowInClientZone, adjusted -> adjusted.plusDays(1)); // Start check from tomorrow if today's time passed
                logger.debug(" WEEKLY | Initial check time (cron will refine): {}", nextReminder);
                // Complex logic could parse pattern.dayOfWeek here, but relies on CronNextExecutionCalculator anyway.
                break;

            case MONTHLY:
                nextReminder = adjustToFuture(targetTime, nowInClientZone, adjusted -> adjusted.plusDays(1)); // Start check from tomorrow
                logger.debug(" MONTHLY | Initial check time (cron will refine): {}", nextReminder);
                // Actual next date depends on dayOfMonth/range which cron parser handles.
                break;

            case YEARLY:
                nextReminder = adjustToFuture(targetTime, nowInClientZone, adjusted -> adjusted.plusDays(1)); // Start check from tomorrow
                logger.debug(" YEARLY | Initial check time (cron will refine): {}", nextReminder);
                // Actual next date depends on month/dayOfMonth which cron parser handles.
                break;

            default:
                logger.error("❌ Unsupported RecurrenceType: {}", type);
                throw new InvalidInputException("Unsupported recurrence type: " + type);
        }

        // 🪵 Log the final calculated ZonedDateTime before returning
        logger.info("✅ Calculated Next Reminder At (Client Zone [{}]): {}", zoneId, nextReminder);
        return nextReminder;
    }

    // Helper to advance the target time if it's already passed today
    private ZonedDateTime adjustToFuture(ZonedDateTime target, ZonedDateTime now, java.util.function.UnaryOperator<ZonedDateTime> advanceLogic) {
        if (target.isBefore(now)) {
            logger.debug("  Adjusting future | Target {} is before now {}, applying advance logic.", target, now);
            ZonedDateTime advanced = advanceLogic.apply(target);
            logger.debug("  Adjusting future | Advanced target: {}", advanced);
            return advanced;
        } else {
            logger.debug("  Adjusting future | Target {} is NOT before now {}, using target.", target, now);
            return target; // Target time is today and hasn't passed yet
        }
    }
}
