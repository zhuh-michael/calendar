package com.starquest.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer amount; // 正数表示收入，负数表示支出

    @Column(nullable = false)
    private String reason;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "related_task_id")
    private Long relatedTaskId;

    @Column(name = "related_reward_id")
    private Long relatedRewardId;

    @Column(name = "related_order_id")
    private Long relatedOrderId;

    public enum TransactionType {
        TASK_REWARD("任务奖励"),
        REWARD_PURCHASE("商品购买"),
        LUCKY_DRAW_COST("抽奖消耗"),
        LUCKY_DRAW_REWARD("抽奖奖励"),
        MANUAL_ADJUSTMENT("人工调整");

        private final String description;

        TransactionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
