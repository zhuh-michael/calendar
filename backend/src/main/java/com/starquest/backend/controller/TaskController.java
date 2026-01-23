package com.starquest.backend.controller;

import com.starquest.backend.model.Task;
import com.starquest.backend.model.TaskEvidence;
import com.starquest.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Value("${app.upload.task-evidence-path}")
    private String taskEvidencePath;

    @GetMapping("/kid/{kidId}")
    public ResponseEntity<List<Task>> getTasksByKidAndDate(
            @PathVariable Long kidId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Task> tasks = taskService.getTasksByKidAndDate(kidId, date);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/kid/{kidId}/pending")
    public ResponseEntity<List<Task>> getPendingTasks(
            @PathVariable Long kidId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Task> tasks = taskService.getPendingTasksByKid(kidId, page, size);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/pending-all")
    public ResponseEntity<List<Task>> getAllPendingTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Task> tasks = taskService.getAllPendingTasks(page, size);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/kid/{kidId}/all")
    public ResponseEntity<List<Task>> getTasksByKidAll(
            @PathVariable Long kidId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Task> tasks = taskService.getTasksByKidAll(kidId, page, size);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Task> tasks = taskService.getAllTasks(page, size);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/query")
    public ResponseEntity<List<Task>> queryTasks(
            @RequestParam(required = false) Long kidId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Task> tasks = taskService.queryTasks(kidId, status, date, page, size);
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
    public ResponseEntity<Void> rejectTask(@PathVariable Long taskId, @RequestParam String rejectReason) {
        taskService.rejectTask(taskId, rejectReason);
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

    @PostMapping("/{taskId}/evidence")
    public ResponseEntity<List<TaskEvidence>> uploadTaskEvidence(
            @PathVariable Long taskId,
            @RequestParam("files") MultipartFile[] files) {
        try {
            // 验证任务存在（并在后续可添加用户权限验证）
            taskService.getTaskById(taskId);
            // TODO: 添加用户权限验证

            // 删除旧的结果数据库记录（覆盖式提交）
            taskService.deleteEvidenceByTaskId(taskId);

            // 保存新的文件 - 按日期分文件夹存储
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            java.util.ArrayList<TaskEvidence> saved = new java.util.ArrayList<>();
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                String fileName = taskId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                String filePath = taskEvidencePath + today + "/" + fileName;
                String fileUrl = "uploads/task-evidence/"  + today + "/" + fileName;
                java.nio.file.Path path = java.nio.file.Paths.get(filePath);
                java.nio.file.Files.createDirectories(path.getParent());
                java.nio.file.Files.write(path, file.getBytes());

                TaskEvidence evidence = taskService.saveTaskEvidence(taskId, fileUrl);
                saved.add(evidence);
            }

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{taskId}/evidence")
    public ResponseEntity<List<TaskEvidence>> getTaskEvidence(@PathVariable Long taskId) {
        List<TaskEvidence> evidence = taskService.getTaskEvidence(taskId);
        return ResponseEntity.ok(evidence);
    }

    @DeleteMapping("/evidence/{evidenceId}")
    public ResponseEntity<Void> deleteTaskEvidence(@PathVariable Long evidenceId) {
        taskService.deleteTaskEvidence(evidenceId);
        return ResponseEntity.ok().build();
    }

    
}
