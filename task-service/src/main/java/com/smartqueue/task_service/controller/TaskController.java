package com.smartqueue.task_service.controller;

import com.smartqueue.task_service.model.Task;
import com.smartqueue.task_service.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    // Submit new task
    @PostMapping("/submit")
    public ResponseEntity<?> submitTask(@RequestBody Task task) {
        try {
            Task savedTask = taskService.submitTask(task);
            return ResponseEntity.ok(savedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get task by ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    // Get all tasks by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getTasksByUser(
            @PathVariable String userId) {
        return ResponseEntity.ok(taskService.getTasksByUser(userId));
    }

    // Get all tasks (Admin)
    @GetMapping("/all")
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }
}