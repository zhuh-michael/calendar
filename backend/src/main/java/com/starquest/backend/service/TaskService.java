package com.starquest.backend.service;

import com.starquest.backend.model.Task;
import com.starquest.backend.model.Transaction;
import com.starquest.backend.repository.TaskRepository;
import com.starquest.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public List<Task> getTasksByKidAndDate(Long kidId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(java.time.LocalTime.MAX);
        return taskRepository.findByKidIdAndStartTimeBetween(kidId, startOfDay, endOfDay);
    }

    public List<Task> getPendingTasksByKid(Long kidId) {
        return taskRepository.findByKidIdAndStatus(kidId, Task.TaskStatus.TODO);
    }

    public List<Task> getTemplates() {
        return taskRepository.findByIsTemplateTrue();
    }

    @Transactional
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }
    
    @Transactional
    public Task updateTask(Long taskId, Task taskDetails) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        task.setTitle(taskDetails.getTitle());
        task.setStartTime(taskDetails.getStartTime());
        task.setRewardStars(taskDetails.getRewardStars());
        task.setNeedsReview(taskDetails.getNeedsReview());
        task.setDescription(taskDetails.getDescription());
        task.setKidId(taskDetails.getKidId());
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        taskRepository.delete(task);
    }

    @Transactional
    public void completeTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        if (!task.getKidId().equals(userId)) {
            throw new RuntimeException("无权操作此任务");
        }

        if (task.getStatus() != Task.TaskStatus.TODO) {
            throw new RuntimeException("任务状态不允许完成");
        }

        if (task.getNeedsReview()) {
            task.setStatus(Task.TaskStatus.PENDING);
        } else {
            task.setStatus(Task.TaskStatus.DONE);
            // 发放奖励
            userService.updateStarBalance(userId, task.getRewardStars());

            // 记录交易
            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setAmount(task.getRewardStars());
            transaction.setReason("完成任务: " + task.getTitle());
            transaction.setRelatedTaskId(taskId);
            transactionRepository.save(transaction);
        }

        taskRepository.save(task);
    }

    @Transactional
    public void approveTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        if (task.getStatus() != Task.TaskStatus.PENDING) {
            throw new RuntimeException("任务状态不允许审核");
        }

        task.setStatus(Task.TaskStatus.DONE);
        taskRepository.save(task);

        // 发放奖励
        userService.updateStarBalance(task.getKidId(), task.getRewardStars());

        // 记录交易
        Transaction transaction = new Transaction();
        transaction.setUserId(task.getKidId());
        transaction.setAmount(task.getRewardStars());
        transaction.setReason("任务审核通过: " + task.getTitle());
        transaction.setRelatedTaskId(taskId);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void rejectTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        if (task.getStatus() != Task.TaskStatus.PENDING) {
            throw new RuntimeException("任务状态不允许审核");
        }

        task.setStatus(Task.TaskStatus.TODO);
        taskRepository.save(task);
    }

    @Transactional
    public void createTasksFromTemplate(Long templateId, Long kidId, LocalDate startDate, LocalDate endDate) {
        Task template = taskRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("模板不存在"));

        if (!template.getIsTemplate()) {
            throw new RuntimeException("这不是一个模板");
        }

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Task newTask = new Task();
            newTask.setTitle(template.getTitle());
            newTask.setKidId(kidId);
            newTask.setStartTime(currentDate.atTime(template.getStartTime().toLocalTime()));
            newTask.setRewardStars(template.getRewardStars());
            newTask.setNeedsReview(template.getNeedsReview());
            newTask.setDescription(template.getDescription());
            newTask.setIsTemplate(false);

            taskRepository.save(newTask);
            currentDate = currentDate.plusDays(1);
        }
    }

    public Long getCompletedTasksCount(Long kidId, LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        return taskRepository.countByKidIdAndStatusAndDate(kidId, Task.TaskStatus.DONE, dateTime);
    }
}
