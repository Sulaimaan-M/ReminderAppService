package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class for converting TimeDetail to ZonedDateTime in client timezone
 */
public class TimeDetailToZonedDateTimeConverter {

    private static final Logger logger = LoggerFactory.getLogger(TimeDetailToZonedDateTimeConverter.class);

    /**
     * Converts TimeDetail to ZonedDateTime representing "today at specified time in client timezone"
     */
    public static ZonedDateTime convert(TimeDetail timeDetail) {
        logger.info("Converting TimeDetail to ZonedDateTime | hours={}, minutes={}, seconds={}, timezone={}",
                timeDetail.hours, timeDetail.minutes, timeDetail.seconds, timeDetail.timezone);

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeDetail.timezone);
            logger.debug("Parsed timezone: {}", zoneId);
        } catch (Exception e) {
            logger.error("Invalid timezone string: {}", timeDetail.timezone, e);
            throw new InvalidInputException("Invalid timezone: " + timeDetail.timezone);
        }

        ZonedDateTime clientTime = ZonedDateTime.now(zoneId)
                .withHour(timeDetail.hours)
                .withMinute(timeDetail.minutes)
                .withSecond(timeDetail.seconds)
                .withNano(0);

        logger.info("Converted to ZonedDateTime: {}", clientTime);
        return clientTime;
    }
}
