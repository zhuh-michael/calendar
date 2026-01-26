package com.starquest.backend.service;

import com.starquest.backend.model.LevelConfig;
import com.starquest.backend.model.StreakBonusConfig;
import com.starquest.backend.model.User;
import com.starquest.backend.repository.LevelConfigRepository;
import com.starquest.backend.repository.StreakBonusConfigRepository;
import com.starquest.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * RPG 等级与成长系统服务
 * 负责经验值获取、等级计算、连签奖励等逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RpgService {

    private final UserRepository userRepository;
    private final LevelConfigRepository levelConfigRepository;
    private final StreakBonusConfigRepository streakBonusConfigRepository;

    /**
     * 获取用户当前等级所需的经验值
     */
    public int getLevelBaseExp(int level) {
        if (level <= 1) return 0;
        Optional<LevelConfig> config = levelConfigRepository.findByLevel(level);
        return config.map(LevelConfig::getRequiredExp).orElse(0);
    }

    /**
     * 获取下一级所需的经验值
     */
    public int getNextLevelBaseExp(int level) {
        return getLevelBaseExp(level + 1);
    }

    /**
     * 获取用户当前等级信息
     */
    public LevelConfig getLevelConfig(int level) {
        return levelConfigRepository.findByLevel(level).orElse(null);
    }

    /**
     * 完成任务时奖励 XP
     * @param xpReward 任务奖励的经验值
     * @return 是否升级
     */
    @Transactional
    public boolean awardTaskXp(Long userId, int xpReward) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        int oldLevel = user.getLevel();
        int oldExp = user.getExp();

        // 增加 XP
        user.setExp(oldExp + xpReward);

        // 检查是否升级
        int newLevel = calculateLevel(user.getExp());
        user.setLevel(newLevel);

        // 更新称号
        LevelConfig newLevelConfig = getLevelConfig(newLevel);
        if (newLevelConfig != null) {
            user.setLevelTitle(newLevelConfig.getTitle());
        }

        userRepository.save(user);

        log.info("用户 {} 完成任务获得 {} XP，当前等级: {} -> {}", userId, xpReward, oldLevel, newLevel);

        return newLevel > oldLevel;
    }

    /**
     * 每日打卡
     * @return 打卡结果信息
     */
    @Transactional
    public CheckInResult checkIn(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        LocalDate today = LocalDate.now();
        LocalDate lastCheckIn = user.getLastCheckinDate();

        int xpGain;
        int starGain;
        String message;
        boolean leveledUp = false;

        // 判断连签状态
        if (lastCheckIn == null || !lastCheckIn.equals(today.minusDays(1))) {
            // 非连续，重置连签天数
            user.setStreakDays(1);
        } else {
            // 连续，递增
            user.setStreakDays(user.getStreakDays() + 1);
        }

        int currentStreak = user.getStreakDays();

        // 获取连签奖励配置（超过7天的循环）
        int bonusDay = currentStreak > 7 ? ((currentStreak - 1) % 7) + 1 : currentStreak;
        Optional<StreakBonusConfig> bonusConfig = streakBonusConfigRepository.findByStreakDays(bonusDay);

        if (bonusConfig.isPresent()) {
            StreakBonusConfig config = bonusConfig.get();
            xpGain = config.getXpBonus();
            starGain = config.getStarBonus();
            message = config.getMessage();
        } else {
            // 默认奖励
            xpGain = 5;
            starGain = 1;
            message = "打卡成功！";
        }

        // 更新用户数据
        int oldLevel = user.getLevel();
        int oldExp = user.getExp();

        user.setExp(oldExp + xpGain);
        user.setStarBalance(user.getStarBalance() + starGain);
        user.setLastCheckinDate(today);

        // 检查升级
        int newLevel = calculateLevel(user.getExp());
        if (newLevel > oldLevel) {
            user.setLevel(newLevel);
            LevelConfig levelConfig = getLevelConfig(newLevel);
            if (levelConfig != null) {
                user.setLevelTitle(levelConfig.getTitle());
            }
            leveledUp = true;
        }

        userRepository.save(user);

        log.info("用户 {} 打卡，连签 {} 天，获得 {} XP, {} 星星，升级: {}", userId, currentStreak, xpGain, starGain, leveledUp);

        return new CheckInResult(xpGain, starGain, message, currentStreak, leveledUp, user.getLevel(), user.getLevelTitle());
    }

    /**
     * 根据累计 XP 计算等级
     * 使用线性查找，XP 越高等级越高
     */
    public int calculateLevel(int totalExp) {
        int level = 1;
        // 从等级10开始向下查找，找到用户当前所处的等级
        for (int i = 10; i >= 1; i--) {
            Optional<LevelConfig> config = levelConfigRepository.findByLevel(i);
            if (config.isPresent() && totalExp >= config.get().getRequiredExp()) {
                return i;
            }
        }
        return level;
    }

    /**
     * 计算当前等级到下一级的进度
     */
    public double calculateLevelProgress(int level, int totalExp) {
        int currentLevelExp = getLevelBaseExp(level);
        int nextLevelExp = getNextLevelBaseExp(level);

        if (nextLevelExp <= currentLevelExp) {
            return 1.0; // 满级
        }

        int progressExp = totalExp - currentLevelExp;
        int requiredExp = nextLevelExp - currentLevelExp;

        return Math.min(1.0, (double) progressExp / requiredExp);
    }

    /**
     * 获取指定等级的头像框样式
     */
    public String getAvatarFrame(int level) {
        LevelConfig config = getLevelConfig(level);
        return config != null ? config.getAvatarFrame() : "bronze";
    }

    /**
     * 打卡结果DTO
     */
    public record CheckInResult(
            int xpGain,
            int starGain,
            String message,
            int streakDays,
            boolean leveledUp,
            int newLevel,
            String newTitle
    ) {}
}

