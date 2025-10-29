package com.sulaimaan.ReminderApp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

/**
 * Entity representing a device's Firebase Cloud Messaging token for push notifications
 */
@Entity
@Getter
@Setter
@Table(name = "device_token", uniqueConstraints = @UniqueConstraint(columnNames = "fcm_token"))
public class DeviceToken {

    private static final Logger logger = LoggerFactory.getLogger(DeviceToken.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fcm_token", nullable = false, unique = true)
    private String fcmToken;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    public DeviceToken() {}

    /**
     * Creates a new DeviceToken with the given FCM token and current timestamp
     */
    public DeviceToken(String fcmToken) {
        this.fcmToken = fcmToken;
        this.createdAt = ZonedDateTime.now();
        logger.debug("DeviceToken created with FCM token length: {}", fcmToken != null ? fcmToken.length() : 0);
    }
}
