package com.sulaimaan.ReminderApp.controller;

import com.sulaimaan.ReminderApp.dto.outgoing.DetailedReminderResponse;
import com.sulaimaan.ReminderApp.service.ReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/reminder")
public class ReminderController {

    private static final Logger logger = LoggerFactory.getLogger(ReminderController.class);

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping("/device/{deviceId}/pending")
    public List<DetailedReminderResponse> getLatestIncompleteByDevice(@PathVariable Long deviceId) {
        logger.info("📋 GET /reminder/device/{}/pending", deviceId);
        List<DetailedReminderResponse> res = reminderService.getLatestIncompleteReminders(deviceId);
        logger.info("✅ GET /reminder/device/{}/pending | Returning {} reminders", deviceId, res.size());
        return res;
    }
}
