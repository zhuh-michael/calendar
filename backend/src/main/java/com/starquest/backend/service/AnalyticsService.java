package com.starquest.backend.service;

import com.starquest.backend.model.Task;
import com.starquest.backend.repository.TaskRepository;
import com.starquest.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TaskRepository taskRepository;
    private final TransactionRepository transactionRepository;

    /**
     * 获取勤奋度曲线数据：近7天每日完成任务数量
     */
    public List<DiligenceDataPoint> getDiligenceCurve(Long kidId) {
        List<DiligenceDataPoint> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            Long completedTasks = taskRepository.countByKidIdAndStatusAndDate(kidId, Task.TaskStatus.DONE, startOfDay);

            result.add(new DiligenceDataPoint(date, completedTasks.intValue()));
        }

        return result;
    }

    /**
     * 获取财富分布数据：累计获得星星 vs 累计消费星星
     */
    public WealthDistributionData getWealthDistribution(Long kidId) {
        List<Object[]> transactions = transactionRepository.findByUserIdOrderByCreateTimeDesc(kidId)
                .stream()
                .map(t -> new Object[]{t.getAmount(), t.getReason()})
                .toList();

        int totalEarned = 0;
        int totalSpent = 0;

        for (Object[] transaction : transactions) {
            Integer amount = (Integer) transaction[0];
            if (amount > 0) {
                totalEarned += amount;
            } else {
                totalSpent += Math.abs(amount);
            }
        }

        return new WealthDistributionData(totalEarned, totalSpent);
    }

    /**
     * 获取今日任务完成率
     */
    public TaskCompletionRate getTodayTaskCompletionRate(Long kidId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Task> todayTasks = taskRepository.findByKidIdAndDate(kidId, startOfDay);
        long totalTasks = todayTasks.size();
        long completedTasks = todayTasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.DONE)
                .count();

        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

        return new TaskCompletionRate(totalTasks, completedTasks, completionRate);
    }

    /**
     * 获取孩子列表及其统计数据（用于家长仪表盘）
     */
    public List<KidStats> getKidsStats() {
        List<KidStats> result = new ArrayList<>();

        // 这里简化处理，实际应该从UserService获取孩子列表
        // 暂时返回空列表，具体实现需要结合UserService

        return result;
    }

    // 数据传输对象

    public static class DiligenceDataPoint {
        private final LocalDate date;
        private final int completedTasks;

        public DiligenceDataPoint(LocalDate date, int completedTasks) {
            this.date = date;
            this.completedTasks = completedTasks;
        }

        public LocalDate getDate() { return date; }
        public int getCompletedTasks() { return completedTasks; }
    }

    public static class WealthDistributionData {
        private final int totalEarned;
        private final int totalSpent;

        public WealthDistributionData(int totalEarned, int totalSpent) {
            this.totalEarned = totalEarned;
            this.totalSpent = totalSpent;
        }

        public int getTotalEarned() { return totalEarned; }
        public int getTotalSpent() { return totalSpent; }
    }

    public static class TaskCompletionRate {
        private final long totalTasks;
        private final long completedTasks;
        private final double completionRate;

        public TaskCompletionRate(long totalTasks, long completedTasks, double completionRate) {
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.completionRate = completionRate;
        }

        public long getTotalTasks() { return totalTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public double getCompletionRate() { return completionRate; }
    }

    public static class KidStats {
        private final Long kidId;
        private final String kidName;
        private final int starBalance;
        private final double todayCompletionRate;
        private final int todayCompletedTasks;

        public KidStats(Long kidId, String kidName, int starBalance, double todayCompletionRate, int todayCompletedTasks) {
            this.kidId = kidId;
            this.kidName = kidName;
            this.starBalance = starBalance;
            this.todayCompletionRate = todayCompletionRate;
            this.todayCompletedTasks = todayCompletedTasks;
        }

        public Long getKidId() { return kidId; }
        public String getKidName() { return kidName; }
        public int getStarBalance() { return starBalance; }
        public double getTodayCompletionRate() { return todayCompletionRate; }
        public int getTodayCompletedTasks() { return todayCompletedTasks; }
    }
}
