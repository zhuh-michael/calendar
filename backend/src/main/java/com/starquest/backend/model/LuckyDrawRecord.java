package com.starquest.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lucky_draw_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LuckyDrawRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kid_id", nullable = false)
    private Long kidId;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", nullable = false)
    private LuckyResultType resultType;

    @Column(nullable = false)
    private Integer starsEarned; // 获得的星星数（正数表示奖励，0表示无奖励）

    @Column(nullable = false)
    private String rewardDescription;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    public enum LuckyResultType {
        GRAND_PRIZE("大吉"),
        GOOD_PRIZE("中吉"),
        SMALL_PRIZE("小吉"),
        ENCOURAGEMENT("鼓励");

        private final String description;

        LuckyResultType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
