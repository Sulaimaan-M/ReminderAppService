package com.sulaimaan.ReminderApp.repository;

import com.sulaimaan.ReminderApp.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for DeviceToken entity database operations
 */
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    /**
     * Finds a DeviceToken by its FCM token string
     */
    DeviceToken findByFcmToken(String fcmToken);
}
