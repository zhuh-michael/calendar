package com.starquest.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "kid_id", nullable = false)
    private Long kidId;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Column(name = "reward_stars", nullable = false)
    private Integer rewardStars;

    @Column(name = "reward_xp")
    private Integer rewardXp;

    @Column(name = "is_template", nullable = false)
    private Boolean isTemplate = false;

    @Column(name = "needs_review", nullable = false)
    private Boolean needsReview = false;

    private String description;

    @Column(name = "reject_reason")
    private String rejectReason;

    public enum TaskStatus {
        TODO(0),    // 待办
        PENDING(1), // 审核中
        DONE(2);    // 已完成

        private final int value;

        TaskStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
