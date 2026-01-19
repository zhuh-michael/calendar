package com.starquest.backend.controller;

import com.starquest.backend.model.Task;
import com.starquest.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/kid/{kidId}")
    public ResponseEntity<List<Task>> getTasksByKidAndDate(
            @PathVariable Long kidId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Task> tasks = taskService.getTasksByKidAndDate(kidId, date);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/kid/{kidId}/pending")
    public ResponseEntity<List<Task>> getPendingTasks(@PathVariable Long kidId) {
        List<Task> tasks = taskService.getPendingTasksByKid(kidId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/templates")
    public ResponseEntity<List<Task>> getTemplates() {
        List<Task> templates = taskService.getTemplates();
        return ResponseEntity.ok(templates);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        return ResponseEntity.ok(createdTask);
    }
    
    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(@PathVariable Long taskId, @RequestBody Task task) {
        Task updated = taskService.updateTask(taskId, task);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Void> completeTask(@PathVariable Long taskId, @RequestParam Long userId) {
        taskService.completeTask(taskId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/approve")
    public ResponseEntity<Void> approveTask(@PathVariable Long taskId) {
        taskService.approveTask(taskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/reject")
    public ResponseEntity<Void> rejectTask(@PathVariable Long taskId) {
        taskService.rejectTask(taskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/from-template")
    public ResponseEntity<Void> createTasksFromTemplate(
            @RequestParam Long templateId,
            @RequestParam Long kidId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        taskService.createTasksFromTemplate(templateId, kidId, startDate, endDate);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/kid/{kidId}/completed-count")
    public ResponseEntity<Long> getCompletedTasksCount(
            @PathVariable Long kidId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long count = taskService.getCompletedTasksCount(kidId, date);
        return ResponseEntity.ok(count);
    }
}
