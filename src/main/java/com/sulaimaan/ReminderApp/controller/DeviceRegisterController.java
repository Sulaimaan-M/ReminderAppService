package com.sulaimaan.ReminderApp.controller;

import com.sulaimaan.ReminderApp.entity.DeviceToken;
import com.sulaimaan.ReminderApp.service.DeviceTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for registering device FCM tokens
 */
@CrossOrigin
@RestController
@RequestMapping("/register")
public class DeviceRegisterController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceRegisterController.class);

    private final DeviceTokenService deviceTokenService;

    @Autowired
    public DeviceRegisterController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    /**
     * Registers a device's Firebase Cloud Messaging token for push notifications
     */
    @PostMapping
    public DeviceToken registerDeviceToken(@RequestBody Map<String, String> request) {
        String token = request.get("fcmToken");
        logger.info("Received device registration request with FCM token length: {}", token == null ? 0 : token.length());

        DeviceToken saved = deviceTokenService.registerDevice(token);

        logger.info("Device registered successfully with ID: {}", saved.getId());
        return saved;
    }
}
