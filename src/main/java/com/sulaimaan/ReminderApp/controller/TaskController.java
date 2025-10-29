package com.sulaimaan.ReminderApp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sulaimaan.ReminderApp.dto.incoming.CreateTaskRequest;
import com.sulaimaan.ReminderApp.dto.incoming.UpdateTaskRequest;
import com.sulaimaan.ReminderApp.dto.outgoing.RecurringTaskResponse;
import com.sulaimaan.ReminderApp.dto.outgoing.SimpleTaskResponse;
import com.sulaimaan.ReminderApp.dto.outgoing.TaskResponse;
import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.service.TaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing tasks (create, update, delete, retrieve)
 */
@CrossOrigin
@RestController
@RequestMapping("/task")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Creates a new task (simple or recurring) and schedules associated reminders
     */
    @PostMapping
    public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request) throws JsonProcessingException {
        logger.info("Received task creation request: {}", objectMapper.writeValueAsString(request));

        Task task = taskService.createTask(request);

        logger.info("Task created successfully with ID: {}", task.getId());
        return TaskResponse.from(task);
    }

    /**
     * Updates an existing task and reschedules its reminders if necessary
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTask(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) throws JsonProcessingException {
        logger.info("Received update request for task ID {}: {}", id, objectMapper.writeValueAsString(request));

        taskService.updateTask(id, request);

        logger.info("Task ID {} updated successfully", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a task and cancels all associated scheduled reminders
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        logger.info("Received deletion request for task ID: {}", id);

        taskService.deleteTask(id);

        logger.info("Task ID {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all recurring tasks for a specific device
     */
    @GetMapping("/device/{deviceId}/recurring")
    public List<RecurringTaskResponse> getRecurringTasks(@PathVariable Long deviceId) {
        logger.info("Fetching recurring tasks for device ID: {}", deviceId);

        List<RecurringTaskResponse> tasks = taskService.getRecurringTasks(deviceId);

        logger.info("Retrieved {} recurring tasks for device ID: {}", tasks.size(), deviceId);
        return tasks;
    }

    /**
     * Retrieves all simple (one-time) tasks for a specific device
     */
    @GetMapping("/device/{deviceId}/simple")
    public List<SimpleTaskResponse> getSimpleTasks(@PathVariable Long deviceId) {
        logger.info("Fetching simple tasks for device ID: {}", deviceId);

        List<SimpleTaskResponse> tasks = taskService.getSimpleTasks(deviceId);

        logger.info("Retrieved {} simple tasks for device ID: {}", tasks.size(), deviceId);
        return tasks;
    }
}
