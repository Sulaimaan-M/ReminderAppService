package com.sulaimaan.ReminderApp.controller;

import com.sulaimaan.ReminderApp.dto.incoming.CreateTaskRequest;
import com.sulaimaan.ReminderApp.dto.incoming.UpdateTaskRequest;
import com.sulaimaan.ReminderApp.dto.outgoing.TaskResponse;
import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.service.TaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/task")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request) {
        logger.info("POST /task | deviceId={} | recurrenceType={}", request.deviceId, request.recurrenceType);
        Task task = taskService.createTask(request);
        logger.info("POST /task | created taskId={}", task.getId());
        return TaskResponse.from(task);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) {
        logger.info("PUT /task/{} | recurrenceType={}", id, request.recurrenceType);
        Task task = taskService.updateTask(id, request);
        logger.info("PUT /task/{} | updated", id);
        return TaskResponse.from(task);
    }

    @DeleteMapping("/{id}")
    public TaskResponse deleteTask(@PathVariable Long id) {
        logger.info("DELETE /task/{}", id);
        Task task = taskService.deleteTask(id);
        logger.info("DELETE /task/{} | deleted", id);
        return TaskResponse.from(task);
    }

    @GetMapping("/device/{deviceId}")
    public List<TaskResponse> getTasksByDevice(@PathVariable Long deviceId) {
        logger.info("GET /task/device/{}", deviceId);
        List<Task> tasks = taskService.getTasksByDevice(deviceId);
        logger.info("GET /task/device/{} | found {}", deviceId, tasks.size());
        return tasks.stream().map(TaskResponse::from).collect(Collectors.toList());
    }
}
