package com.sulaimaan.ReminderApp.controller;

import com.sulaimaan.ReminderApp.dto.outgoing.PendingReminderResponse;
import com.sulaimaan.ReminderApp.service.ReminderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/reminder")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping("/device/{deviceId}/pending")
    public List<PendingReminderResponse> getLatestIncompleteByDevice(@PathVariable Long deviceId) {
        return reminderService.getLatestIncompleteByDevice(deviceId);
    }
}
