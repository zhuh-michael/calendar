package com.starquest.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "streak_bonus_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreakBonusConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "streak_days", nullable = false, unique = true)
    private Integer streakDays;

    @Column(name = "xp_bonus", nullable = false)
    private Integer xpBonus;

    @Column(name = "star_bonus", nullable = false)
    private Integer starBonus;

    @Column(nullable = false)
    private String message;
}

