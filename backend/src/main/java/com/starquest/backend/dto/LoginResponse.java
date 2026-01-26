package com.starquest.backend.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private Long userId;
    private String username;
    private String role;
    private String nickname;
    private Integer starBalance;
    private String token;

    // RPG 字段
    private Integer exp;
    private Integer level;
    private String levelTitle;
    private Integer streakDays;
    private String avatarFrame;

    public LoginResponse(Long userId, String username, String role, String nickname, Integer starBalance) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.nickname = nickname;
        this.starBalance = starBalance;
    }
}
