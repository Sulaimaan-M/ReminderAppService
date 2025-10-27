package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.entity.DeviceToken;
import com.sulaimaan.ReminderApp.repository.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceTokenService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceTokenService.class);

    private final DeviceTokenRepository deviceTokenRepo;

    @Autowired
    public DeviceTokenService(DeviceTokenRepository deviceTokenRepo) {
        this.deviceTokenRepo = deviceTokenRepo;
    }

    public DeviceToken registerDevice(String fcmToken) {
        logger.info("DeviceTokenService.registerDevice | tokenLen={}", fcmToken == null ? 0 : fcmToken.length());

        DeviceToken existing = deviceTokenRepo.findByFcmToken(fcmToken);
        if (existing != null) {
            logger.info("DeviceTokenService.registerDevice | exists id={}", existing.getId());
            return existing;
        }

        DeviceToken newToken = new DeviceToken(fcmToken);
        DeviceToken saved = deviceTokenRepo.save(newToken);
        logger.info("DeviceTokenService.registerDevice | saved id={}", saved.getId());
        return saved;
    }

    public DeviceToken getDeviceTokenById(Long id) {
        logger.info("DeviceTokenService.getDeviceTokenById | id={}", id);
        return deviceTokenRepo.findById(id).orElse(null);
    }
}
