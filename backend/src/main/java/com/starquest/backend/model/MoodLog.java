package com.starquest.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mood_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kid_id", nullable = false)
    private Long kidId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood_type", nullable = false)
    private MoodType moodType;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    private String note;

    public enum MoodType {
        HAPPY("开心"),
        NEUTRAL("一般"),
        ANGRY("生气"),
        SAD("难过");

        private final String description;

        MoodType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
