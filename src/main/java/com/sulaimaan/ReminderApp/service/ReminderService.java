package com.sulaimaan.ReminderApp.service;

import com.sulaimaan.ReminderApp.dto.outgoing.PendingReminderResponse;
import com.sulaimaan.ReminderApp.entity.Reminder;
import com.sulaimaan.ReminderApp.repository.ReminderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);

    private final ReminderRepository reminderRepository;

    public ReminderService(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    public List<PendingReminderResponse> getLatestIncompleteByDevice(Long deviceId) {
        logger.info("🔵 ReminderService.getLatestIncompleteByDevice | deviceId={}", deviceId);
        List<Reminder> reminders = reminderRepository.findLatestIncompleteByDevice(deviceId);
        logger.info("🔵 ReminderService.getLatestIncompleteByDevice | deviceId={} found {} reminders", deviceId, reminders.size());
        return reminders.stream().map(PendingReminderResponse::from).collect(Collectors.toList());
    }
}
