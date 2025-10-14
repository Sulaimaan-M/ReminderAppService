package com.sulaimaan.ReminderApp.controller;

import com.sulaimaan.ReminderApp.dto.ReminderDTO;
import com.sulaimaan.ReminderApp.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reminder")
public class ReminderController {

    private final ReminderService service;

    @Autowired
    public ReminderController(ReminderService service){

        this.service = service;

    }

    @PostMapping
    public boolean newReminder(@Valid @RequestBody ReminderDTO reminderDTO){

        return service.createReminder(reminderDTO);


    }

}
