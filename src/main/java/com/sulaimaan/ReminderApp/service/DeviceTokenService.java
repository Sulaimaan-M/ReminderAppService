// com.sulaimaan.ReminderApp.service.DeviceTokenService.java
package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.entity.DeviceToken;
import com.sulaimaan.ReminderApp.repository.DeviceTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepo;

    @Autowired
    public DeviceTokenService(DeviceTokenRepository deviceTokenRepo) {
        this.deviceTokenRepo = deviceTokenRepo;
    }

    public DeviceToken registerDevice(String fcmToken) {
        // Check if token already exists
        DeviceToken existing = deviceTokenRepo.findByFcmToken(fcmToken); // ← RENAMED
        if (existing != null) {
            return existing;
        }

        DeviceToken newToken = new DeviceToken(fcmToken); // ← Now uses FCM constructor
        return deviceTokenRepo.save(newToken);
    }

    // In DeviceTokenService.java
    public DeviceToken getDeviceTokenById(Long id) {
        return deviceTokenRepo.findById(id).orElse(null);
    }
}
