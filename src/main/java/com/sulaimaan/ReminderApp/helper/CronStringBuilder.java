package com.sulaimaan.ReminderApp.helper;

import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

public class CronStringBuilder {

    public static String build(ZonedDateTime time, IntervalType interval) {
        return switch (interval) {
            case DAILY -> buildDaily(time);
            case WEEKLY -> buildWeekly(time);
            case MONTHLY -> buildMonthly(time);
            case YEARLY -> buildYearly(time);
            default -> throw new IllegalStateException("Unexpected value: " + interval);
        };
    }

    private static String buildDaily(ZonedDateTime time) {
        int second = time.getSecond();
        int minute = time.getMinute();
        int hour = time.getHour();
        // Daily: every day at HH:MM:SS
        return String.format("%d %d %d * * ?", second, minute, hour);
    }

    private static String buildWeekly(ZonedDateTime time) {
        int second = time.getSecond();
        int minute = time.getMinute();
        int hour = time.getHour();
        // Get day of week as 3-letter uppercase (e.g., MON, TUE) — Quartz accepts this
        String dayOfWeek = time.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
        // Weekly: every week on this day at HH:MM:SS
        return String.format("%d %d %d ? * %s", second, minute, hour, dayOfWeek);
    }

    private static String buildMonthly(ZonedDateTime time) {
        int second = time.getSecond();
        int minute = time.getMinute();
        int hour = time.getHour();
        int dayOfMonth = time.getDayOfMonth();
        // Monthly: every month on this day at HH:MM:SS
        // Note: If day > 28, Quartz will skip months with fewer days (e.g., Feb 30 → skipped)
        return String.format("%d %d %d %d * ?", second, minute, hour, dayOfMonth);
    }

    private static String buildYearly(ZonedDateTime time) {
        int second = time.getSecond();
        int minute = time.getMinute();
        int hour = time.getHour();
        int dayOfMonth = time.getDayOfMonth();
        int month = time.getMonthValue(); // 1–12
        // Yearly: every year on this month/day at HH:MM:SS
        // This works because (day=15, month=3) only occurs once per year
        return String.format("%d %d %d %d %d ?", second, minute, hour, dayOfMonth, month);
    }
}
