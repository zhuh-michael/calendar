package com.starquest.backend.dto;

import lombok.Data;

@Data
public class CreateKidRequest {
    private String username;
    private String password;
    private String nickname;
}
