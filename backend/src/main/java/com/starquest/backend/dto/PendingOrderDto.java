package com.starquest.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PendingOrderDto {
    private Long id;
    private Long kidId;
    private String kidNickname;
    private Long rewardId;
    private String rewardName;
    private LocalDateTime createTime;
    private Integer status;
}


