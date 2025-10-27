package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeConverter {

    private static final Logger logger = LoggerFactory.getLogger(TimeConverter.class); // 🪵 Logger instance

    public static LocalTime convertToUtc(TimeDetail timeDetail) {
        // 🪵 Log the input time detail received from the client
        logger.info("🕰️➡️ UTC | Input TimeDetail: hours={}, minutes={}, seconds={}, timezone={}",
                timeDetail.hours, timeDetail.minutes, timeDetail.seconds, timeDetail.timezone); // Use field access

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeDetail.timezone); // Use field access
            // 🪵 Log the identified ZoneId
            logger.debug("🌍 Identified ZoneId: {}", zoneId);
        } catch (Exception e) {
            // 🪵 Log error if the timezone string is invalid
            logger.error("❌ Invalid timezone string provided: {}", timeDetail.timezone, e); // Use field access
            throw new InvalidInputException("Invalid timezone: " + timeDetail.timezone); // Use field access
        }

        // Create a ZonedDateTime representing "today" at the client's specified time and zone
        ZonedDateTime clientTime = ZonedDateTime.now(zoneId)
                .withHour(timeDetail.hours) // Use field access
                .withMinute(timeDetail.minutes) // Use field access
                .withSecond(timeDetail.seconds) // Use field access
                .withNano(0); // Ensure nano is 0 for consistency

        // 🪵 Log the created ZonedDateTime in the client's timezone
        logger.debug("📅 Client's ZonedDateTime (conceptual 'today'): {}", clientTime);

        // Convert this time to UTC
        ZonedDateTime utcTime = clientTime.withZoneSameInstant(ZoneId.of("UTC"));

        // 🪵 Log the resulting ZonedDateTime in UTC
        logger.debug(" UTC ZonedDateTime: {}", utcTime);

        // Extract only the time part (hour, minute, second) from the UTC ZonedDateTime
        LocalTime utcLocalTime = utcTime.toLocalTime();

        // 🪵 Log the final UTC LocalTime being returned for cron calculation
        logger.info("✅ UTC | Output LocalTime for Cron: {}", utcLocalTime);
        return utcLocalTime;
    }
}
