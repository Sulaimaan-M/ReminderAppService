package com.sulaimaan.ReminderApp.repository;

import com.sulaimaan.ReminderApp.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    DeviceToken findByFcmToken(String fcmToken);
}
