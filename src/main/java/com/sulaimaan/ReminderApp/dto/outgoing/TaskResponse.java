package com.sulaimaan.ReminderApp.dto.outgoing;

import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.helper.RecurrenceType;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

import java.time.ZoneId; // Import ZoneId
import java.time.ZonedDateTime;

@Data
@Builder
public class TaskResponse {

    private static final Logger logger = LoggerFactory.getLogger(TaskResponse.class); // 🪵 Logger instance

    private Long id;
    private String taskText;
    private ZonedDateTime createdAt;
    private ZonedDateTime nextReminderAt; // This should be in the client's original timezone
    private String cronExpression; // This remains UTC based for info/debug
    private RecurrenceType recurrenceType;
    private Long deviceId;

    public static TaskResponse from(Task task) {
        // 🪵 Log the raw Task data being converted
        logger.debug("🔄 TaskResponse.from | Converting Task | id={}, nextReminderAt='{}', clientTimezone='{}'",
                task.getId(), task.getNextReminderAt(), task.getClientTimezone());

        ZonedDateTime nextReminderAtInClientZone = task.getNextReminderAt(); // Start with the retrieved time

        String clientTimezoneId = task.getClientTimezone();
        if (clientTimezoneId != null && !clientTimezoneId.isEmpty()) {
            try {
                ZoneId clientZone = ZoneId.of(clientTimezoneId);
                // Convert the retrieved instant (likely UTC or server default) to the stored client timezone
                nextReminderAtInClientZone = task.getNextReminderAt().withZoneSameInstant(clientZone);
                // 🪵 Log the successful conversion
                logger.debug("  🔄 Converted nextReminderAt to client zone [{}] -> {}", clientTimezoneId, nextReminderAtInClientZone);
            } catch (Exception e) {
                // 🪵 Log if the stored timezone is invalid, fallback to original ZonedDateTime
                logger.error("❌ TaskResponse.from | Failed to parse stored clientTimezone '{}' for taskId={}. Using original retrieved ZonedDateTime. Error: {}",
                        clientTimezoneId, task.getId(), e.getMessage());
                // nextReminderAtInClientZone remains the originally retrieved ZonedDateTime
            }
        } else {
            // 🪵 Log if no client timezone was stored for this task
            logger.warn("⚠️ TaskResponse.from | clientTimezone is null or empty for taskId={}. Using original retrieved ZonedDateTime.", task.getId());
            // nextReminderAtInClientZone remains the originally retrieved ZonedDateTime
        }

        TaskResponse response = TaskResponse.builder()
                .id(task.getId())
                .taskText(task.getTaskTxt())
                .createdAt(task.getCreatedAt()) // Assuming createdAt should also be consistent (e.g., UTC)
                .nextReminderAt(nextReminderAtInClientZone) // Use the converted (or original if failed) time
                .cronExpression(task.getCronExpression())
                .recurrenceType(task.getRecurrenceType())
                .deviceId(task.getDeviceToken().getId())
                .build();

        // 🪵 Log the final TaskResponse before serialization
        logger.debug("✅ TaskResponse.from | Final Response Object | id={}, nextReminderAt='{}'",
                response.getId(), response.getNextReminderAt());
        return response;
    }
}
