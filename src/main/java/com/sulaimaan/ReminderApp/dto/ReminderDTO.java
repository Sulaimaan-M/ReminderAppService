package com.sulaimaan.ReminderApp.dto;

import com.sulaimaan.ReminderApp.helper.IntervalType;

import java.time.ZonedDateTime;

public class ReminderDTO {

    public String reminderTxt;

    public ZonedDateTime remindAt;

    public IntervalType interval;

    public Long deviceTokenId;
}
