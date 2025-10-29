package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class for converting client timezone time to UTC for scheduling purposes
 */
public class TimeConverter {

    private static final Logger logger = LoggerFactory.getLogger(TimeConverter.class);

    /**
     * Converts client time details from their timezone to UTC time components
     */
    public static LocalTime convertToUtc(TimeDetail timeDetail) {
        logger.info("🕰️➡️ UTC | Input TimeDetail: hours={}, minutes={}, seconds={}, timezone={}",
                timeDetail.hours, timeDetail.minutes, timeDetail.seconds, timeDetail.timezone);

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeDetail.timezone);
            logger.debug("🌍 Identified ZoneId: {}", zoneId);
        } catch (Exception e) {
            logger.error("❌ Invalid timezone string provided: {}", timeDetail.timezone, e);
            throw new InvalidInputException("Invalid timezone: " + timeDetail.timezone);
        }

        ZonedDateTime clientTime = ZonedDateTime.now(zoneId)
                .withHour(timeDetail.hours)
                .withMinute(timeDetail.minutes)
                .withSecond(timeDetail.seconds)
                .withNano(0);

        logger.debug("📅 Client's ZonedDateTime (conceptual 'today'): {}", clientTime);

        ZonedDateTime utcTime = clientTime.withZoneSameInstant(ZoneId.of("UTC"));

        logger.debug(" UTC ZonedDateTime: {}", utcTime);

        LocalTime utcLocalTime = utcTime.toLocalTime();

        logger.info("✅ UTC | Output LocalTime for Cron: {}", utcLocalTime);
        return utcLocalTime;
    }
}
