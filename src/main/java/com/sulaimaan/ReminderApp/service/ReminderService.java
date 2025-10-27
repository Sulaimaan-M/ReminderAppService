package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.dto.outgoing.PendingReminderResponse;
import com.sulaimaan.ReminderApp.entity.Reminder;
import com.sulaimaan.ReminderApp.repository.ReminderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;

    public ReminderService(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    public List<PendingReminderResponse> getLatestIncompleteByDevice(Long deviceId) {
        List<Reminder> reminders = reminderRepository.findLatestIncompleteByDevice(deviceId);
        return reminders.stream()
                .map(PendingReminderResponse::from)
                .collect(Collectors.toList());
    }
}
