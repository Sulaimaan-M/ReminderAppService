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

    // Saving the token in the Database.
    public DeviceToken registerDevice(String fcmToken) {

        DeviceToken existing = deviceTokenRepo.findByFcmToken(fcmToken);
        if (existing != null) {
            return existing;
        }

        DeviceToken newToken = new DeviceToken(fcmToken); // ← Now uses FCM constructor
        return deviceTokenRepo.save(newToken);
    }

    public DeviceToken getDeviceTokenById(Long id) {
        return deviceTokenRepo.findById(id).orElse(null);
    }
}
