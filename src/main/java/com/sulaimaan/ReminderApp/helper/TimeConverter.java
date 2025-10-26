package com.sulaimaan.ReminderApp.helper;

import com.sulaimaan.ReminderApp.dto.incoming.minor.TimeDetail;
import com.sulaimaan.ReminderApp.exception_handling.exception.InvalidInputException;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TimeConverter {

    public static class UtcTime {
        public final int hours;
        public final int minutes;
        public final int seconds;

        public UtcTime(int hours, int minutes, int seconds) {
            this.hours = hours;
            this.minutes = minutes;
            this.seconds = seconds;
        }
    }

    public UtcTime convertToUtc(TimeDetail timeDetail) {
        try {
            ZoneId clientZone = ZoneId.of(timeDetail.timezone);

            ZonedDateTime nowInClientZone = ZonedDateTime.now(clientZone)
                    .withHour(timeDetail.hours)
                    .withMinute(timeDetail.minutes)
                    .withSecond(timeDetail.seconds)
                    .withNano(0);

            ZonedDateTime inUtc = nowInClientZone.withZoneSameInstant(ZoneOffset.UTC);

            return new UtcTime(inUtc.getHour(), inUtc.getMinute(), inUtc.getSecond());

        } catch (Exception e) {
            throw new InvalidInputException("Invalid timezone: " + timeDetail.timezone);
        }
    }
}
