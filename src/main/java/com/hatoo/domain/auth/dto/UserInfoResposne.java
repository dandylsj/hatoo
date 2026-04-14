package com.hatoo.domain.auth.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class UserInfoResposne {

    private final UUID id;
    private final String status;
    private final String loginId;
    private final String email;
    private final String nickname;
    private final String profileImg;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public UserInfoResposne(UUID id, String status, String loginId, String email, String nickname, String profileImg, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.status = status;
        this.loginId = loginId;
        this.email = email;
        this.nickname = nickname;
        this.profileImg = profileImg;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
