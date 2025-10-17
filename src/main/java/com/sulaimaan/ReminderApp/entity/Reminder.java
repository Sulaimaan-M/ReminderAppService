package com.sulaimaan.ReminderApp.entity;

import com.sulaimaan.ReminderApp.helper.IntervalType;
import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "reminder")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String text;

    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime remindAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntervalType intervalType;

    @ManyToOne
    @JoinColumn(name = "device_token_id")
    private DeviceToken deviceToken;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getRemindAt() {
        return remindAt;
    }

    public void setRemindAt(ZonedDateTime remindAt) {
        this.remindAt = remindAt;
    }

    public IntervalType getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(IntervalType intervalType) {
        this.intervalType = intervalType;
    }

    public DeviceToken getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(DeviceToken deviceToken) {
        this.deviceToken = deviceToken;
    }
}
