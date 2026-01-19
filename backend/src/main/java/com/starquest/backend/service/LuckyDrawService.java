package com.starquest.backend.service;

import com.starquest.backend.model.LuckyDrawRecord;
import com.starquest.backend.model.Transaction;
import com.starquest.backend.model.User;
import com.starquest.backend.repository.LuckyDrawRecordRepository;
import com.starquest.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class LuckyDrawService {

    private final UserService userService;
    private final LuckyDrawRecordRepository luckyDrawRecordRepository;
    private final TransactionRepository transactionRepository;

    private static final int DRAW_COST = 20; // 每次抽奖消耗20星星
    private static final Random random = new Random();

    // 奖池配置
    private static final LuckyDrawConfig[] PRIZE_POOL = {
        new LuckyDrawConfig(LuckyDrawRecord.LuckyResultType.GRAND_PRIZE, 5, 100, "获得100星星大礼包！"),     // 5%
        new LuckyDrawConfig(LuckyDrawRecord.LuckyResultType.GOOD_PRIZE, 20, 30, "获得免做卡一张！"),         // 20%
        new LuckyDrawConfig(LuckyDrawRecord.LuckyResultType.SMALL_PRIZE, 45, 10, "获得10星星回血！"),       // 45%
        new LuckyDrawConfig(LuckyDrawRecord.LuckyResultType.ENCOURAGEMENT, 30, 0, "谢谢参与！继续加油！")    // 30%
    };

    @Transactional
    public LuckyDrawResult performLuckyDraw(Long kidId) {
        User kid = userService.getUserById(kidId);

        // 检查余额
        if (kid.getStarBalance() < DRAW_COST) {
            throw new RuntimeException("星星余额不足，需要" + DRAW_COST + "星星");
        }

        // 扣除抽奖费用
        userService.updateStarBalance(kidId, -DRAW_COST);

        // 记录抽奖消费
        Transaction costTransaction = new Transaction();
        costTransaction.setUserId(kidId);
        costTransaction.setAmount(-DRAW_COST);
        costTransaction.setReason("幸运屋抽奖消费");
        transactionRepository.save(costTransaction);

        // 随机抽奖
        LuckyDrawConfig result = drawPrize();

        // 如果中奖，发放奖励
        if (result.getStarsEarned() > 0) {
            userService.updateStarBalance(kidId, result.getStarsEarned());

            // 记录奖励交易
            Transaction rewardTransaction = new Transaction();
            rewardTransaction.setUserId(kidId);
            rewardTransaction.setAmount(result.getStarsEarned());
            rewardTransaction.setReason("幸运屋抽奖奖励: " + result.getDescription());
            transactionRepository.save(rewardTransaction);
        }

        // 记录抽奖结果
        LuckyDrawRecord record = new LuckyDrawRecord();
        record.setKidId(kidId);
        record.setResultType(result.getType());
        record.setStarsEarned(result.getStarsEarned());
        record.setRewardDescription(result.getDescription());
        luckyDrawRecordRepository.save(record);

        return new LuckyDrawResult(
            result.getType(),
            result.getStarsEarned(),
            result.getDescription(),
            kid.getStarBalance() - DRAW_COST + result.getStarsEarned() // 更新后的余额
        );
    }

    private LuckyDrawConfig drawPrize() {
        int totalWeight = 0;
        for (LuckyDrawConfig config : PRIZE_POOL) {
            totalWeight += config.getWeight();
        }

        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (LuckyDrawConfig config : PRIZE_POOL) {
            currentWeight += config.getWeight();
            if (randomValue < currentWeight) {
                return config;
            }
        }

        // 默认返回鼓励奖
        return PRIZE_POOL[PRIZE_POOL.length - 1];
    }

    public List<LuckyDrawRecord> getDrawHistory(Long kidId) {
        return luckyDrawRecordRepository.findByKidIdOrderByCreateTimeDesc(kidId);
    }

    // 内部配置类
    private static class LuckyDrawConfig {
        private final LuckyDrawRecord.LuckyResultType type;
        private final int weight;
        private final int starsEarned;
        private final String description;

        public LuckyDrawConfig(LuckyDrawRecord.LuckyResultType type, int weight, int starsEarned, String description) {
            this.type = type;
            this.weight = weight;
            this.starsEarned = starsEarned;
            this.description = description;
        }

        public LuckyDrawRecord.LuckyResultType getType() { return type; }
        public int getWeight() { return weight; }
        public int getStarsEarned() { return starsEarned; }
        public String getDescription() { return description; }
    }

    // 返回结果类
    public static class LuckyDrawResult {
        private final LuckyDrawRecord.LuckyResultType resultType;
        private final int starsEarned;
        private final String description;
        private final int newBalance;

        public LuckyDrawResult(LuckyDrawRecord.LuckyResultType resultType, int starsEarned, String description, int newBalance) {
            this.resultType = resultType;
            this.starsEarned = starsEarned;
            this.description = description;
            this.newBalance = newBalance;
        }

        public LuckyDrawRecord.LuckyResultType getResultType() { return resultType; }
        public int getStarsEarned() { return starsEarned; }
        public String getDescription() { return description; }
        public int getNewBalance() { return newBalance; }
    }
}
