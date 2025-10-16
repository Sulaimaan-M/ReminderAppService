// com.sulaimaan.ReminderApp.controller.DeviceRegisterController.java
package com.sulaimaan.ReminderApp.controller;

import com.sulaimaan.ReminderApp.dto.TokenRequestDTO;
import com.sulaimaan.ReminderApp.entity.DeviceToken;
import com.sulaimaan.ReminderApp.service.DeviceTokenService;
import jakarta.validation.Valid;
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

    @PostMapping
    public DeviceToken registerDeviceToken(@Valid @RequestBody TokenRequestDTO request) {

        System.out.println("Registering device token : "+request.fcmToken);
        return deviceTokenService.registerDevice(request.fcmToken);
    }
}
