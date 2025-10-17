package com.sulaimaan.ReminderApp.controller;

import com.sulaimaan.ReminderApp.entity.DeviceToken;
import com.sulaimaan.ReminderApp.service.DeviceTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/register")
public class DeviceRegisterController {

    private final DeviceTokenService deviceTokenService;

    @Autowired
    public DeviceRegisterController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    // Gets invoked at the start-up of the Front-end,
    // Is a necessary Data point to identify reminders and
    // to send out Notifications
    @PostMapping
    public DeviceToken registerDeviceToken( @RequestBody String fcmToken) {

        System.out.println("📥 Registering device token : "+fcmToken);
        return deviceTokenService.registerDevice(fcmToken);
    }
}
