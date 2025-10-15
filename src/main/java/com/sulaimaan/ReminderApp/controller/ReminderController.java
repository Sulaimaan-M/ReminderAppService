// com.sulaimaan.ReminderApp.controller.ReminderController.java
package com.sulaimaan.ReminderApp.controller;

import com.sulaimaan.ReminderApp.dto.ReminderDTO;
import com.sulaimaan.ReminderApp.dto.TokenRequestDTO;
import com.sulaimaan.ReminderApp.entity.Reminder;
import com.sulaimaan.ReminderApp.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reminder")
public class ReminderController {

    private final ReminderService service;

    @Autowired
    public ReminderController(ReminderService service) {
        this.service = service;
    }

    // CREATE
    @PostMapping
    public boolean newReminder(@Valid @RequestBody ReminderDTO reminderDTO) {
        return service.createReminder(reminderDTO);
    }

    // UPDATE (EDIT)
    @PutMapping("/{id}")
    public boolean updateReminder(
            @PathVariable Long id,
            @Valid @RequestBody ReminderDTO reminderDTO) {
        return service.updateReminder(id, reminderDTO);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public boolean deleteReminder(@PathVariable Long id) {
        return service.deleteReminder(id);
    }

    // GET ALL REMINDERS BY FCM TOKEN
    @PostMapping("/by-token")
    public List<Reminder> getRemindersByToken(@RequestBody TokenRequestDTO request) {
        return service.getRemindersByToken(request.fcmToken);
    }
}
