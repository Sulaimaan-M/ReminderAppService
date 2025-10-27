package com.sulaimaan.ReminderApp.controller;

import com.sulaimaan.ReminderApp.dto.incoming.CreateTaskRequest;
import com.sulaimaan.ReminderApp.dto.incoming.UpdateTaskRequest;
import com.sulaimaan.ReminderApp.dto.outgoing.TaskResponse;
import com.sulaimaan.ReminderApp.entity.Task;
import com.sulaimaan.ReminderApp.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request) {
        System.out.println("📝 Creating task: " + request.taskText + " for device: " + request.deviceId);
        Task task = taskService.createTask(request);
        return TaskResponse.from(task);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) {
        System.out.println("✏️ Updating task: " + id);
        Task task = taskService.updateTask(id, request);
        return TaskResponse.from(task);
    }

    @DeleteMapping("/{id}")
    public TaskResponse deleteTask(@PathVariable Long id) {
        System.out.println("🗑️ Deleting task: " + id);
        Task task = taskService.deleteTask(id);
        return TaskResponse.from(task);
    }

    @GetMapping("/device/{deviceId}")
    public List<TaskResponse> getTasksByDevice(@PathVariable Long deviceId) {
        System.out.println("📋 Getting tasks for device: " + deviceId);
        List<Task> tasks = taskService.getTasksByDevice(deviceId);
        System.out.println("✅ Found " + tasks.size() + " tasks for device: " + deviceId);
        return tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }
}
