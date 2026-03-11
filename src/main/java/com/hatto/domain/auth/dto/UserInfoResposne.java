package com.hatto.domain.auth.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserInfoResposne {

    private Long id;
    private String status;
    private String loginId;
    private String email;
    private String nickname;
    private String profileImg;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String fcmToken;

    public UserInfoResposne(Long id, String status, String loginId, String email, String nickname, String profileImg, LocalDateTime createdAt, LocalDateTime updatedAt) {
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
