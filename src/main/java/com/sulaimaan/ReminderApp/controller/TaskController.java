package com.sulaimaan.ReminderApp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sulaimaan.ReminderApp.dto.incoming.CreateTaskRequest;
import com.sulaimaan.ReminderApp.dto.incoming.UpdateTaskRequest;
import com.sulaimaan.ReminderApp.dto.outgoing.TaskResponse;
import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.service.TaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

    @PostMapping
    public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request) throws JsonProcessingException {
        logger.info("✨ POST /task | Received: {}", objectMapper.writeValueAsString(request));
        Task task = taskService.createTask(request);
        logger.info("✅ POST /task | Created taskId={}", task.getId());
        return TaskResponse.from(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTask(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) throws JsonProcessingException {
        logger.info("✏️ PUT /task/{} | Received: {}", id, objectMapper.writeValueAsString(request));
        taskService.updateTask(id, request);
        logger.info("✅ PUT /task/{} | Updated (204 No Content)", id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        logger.info("🗑️ DELETE /task/{}", id);
        taskService.deleteTask(id);
        logger.info("✅ DELETE /task/{} | Deleted (204 No Content)", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/device/{deviceId}")
    public List<TaskResponse> getTasksByDevice(@PathVariable Long deviceId) {
        logger.info("📋 GET /task/device/{}", deviceId);
        List<Task> tasks = taskService.getTasksByDevice(deviceId);
        logger.info("✅ GET /task/device/{} | Returning {} tasks", deviceId, tasks.size());
        return tasks.stream().map(TaskResponse::from).collect(Collectors.toList());
    }
}
