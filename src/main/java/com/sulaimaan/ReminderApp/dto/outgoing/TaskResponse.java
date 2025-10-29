package com.sulaimaan.ReminderApp.dto.outgoing;

import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.helper.RecurrenceType;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Response DTO for task information with timezone-aware reminder scheduling
 */
@Data
@Builder
public class TaskResponse {

    private static final Logger logger = LoggerFactory.getLogger(TaskResponse.class);

    private Long id;
    private String taskText;
    private ZonedDateTime createdAt;
    private ZonedDateTime nextReminderAt;
    private String cronExpression;
    private RecurrenceType recurrenceType;
    private Long deviceId;

    /**
     * Converts a Task entity to TaskResponse DTO with client timezone conversion
     */
    public static TaskResponse from(Task task) {
        logger.debug("🔄 TaskResponse.from | Converting Task | id={}, nextReminderAt='{}', clientTimezone='{}'",
                task.getId(), task.getNextReminderAt(), task.getClientTimezone());

        ZonedDateTime nextReminderAtInClientZone = task.getNextReminderAt();

        String clientTimezoneId = task.getClientTimezone();
        if (clientTimezoneId != null && !clientTimezoneId.isEmpty()) {
            try {
                ZoneId clientZone = ZoneId.of(clientTimezoneId);
                nextReminderAtInClientZone = task.getNextReminderAt().withZoneSameInstant(clientZone);
                logger.debug("  🔄 Converted nextReminderAt to client zone [{}] -> {}", clientTimezoneId, nextReminderAtInClientZone);
            } catch (Exception e) {
                logger.error("❌ TaskResponse.from | Failed to parse stored clientTimezone '{}' for taskId={}. Using original retrieved ZonedDateTime. Error: {}",
                        clientTimezoneId, task.getId(), e.getMessage());
            }
        } else {
            logger.warn("⚠️ TaskResponse.from | clientTimezone is null or empty for taskId={}. Using original retrieved ZonedDateTime.", task.getId());
        }

        TaskResponse response = TaskResponse.builder()
                .id(task.getId())
                .taskText(task.getTaskTxt())
                .createdAt(task.getCreatedAt())
                .nextReminderAt(nextReminderAtInClientZone)
                .cronExpression(task.getCronExpression())
                .recurrenceType(task.getRecurrenceType())
                .deviceId(task.getDeviceToken().getId())
                .build();

        logger.debug("✅ TaskResponse.from | Final Response Object | id={}, nextReminderAt='{}'",
                response.getId(), response.getNextReminderAt());
        return response;
    }
}
