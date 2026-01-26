package com.starquest.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "star_balance", nullable = false)
    private Integer starBalance = 0;

    private String avatar;

    private String nickname;

    // RPG 核心字段
    @Column(nullable = false)
    private Integer exp = 0;

    @Column(nullable = false)
    private Integer level = 1;

    @Column(name = "level_title")
    private String levelTitle = "星际见习生";

    @Column(name = "streak_days", nullable = false)
    private Integer streakDays = 0;

    @Column(name = "last_checkin_date")
    private java.time.LocalDate lastCheckinDate;

    public enum UserRole {
        PARENT, KID
    }
}
