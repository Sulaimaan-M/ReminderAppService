// com.sulaimaan.ReminderApp.entity.DeviceToken.java
package com.sulaimaan.ReminderApp.entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "device_token", uniqueConstraints = @UniqueConstraint(columnNames = "fcm_token"))
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fcm_token", nullable = false, unique = true, length = 255)
    private String fcmToken; // ← RENAMED from apnsToken

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    // Default constructor (required by JPA)
    public DeviceToken() {}

    // Convenience constructor
    public DeviceToken(String fcmToken) { // ← RENAMED parameter
        this.fcmToken = fcmToken; // ← RENAMED
        this.createdAt = ZonedDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getFcmToken() { // ← RENAMED
        return fcmToken;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setFcmToken(String fcmToken) { // ← RENAMED
        this.fcmToken = fcmToken;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
