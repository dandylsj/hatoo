package com.hatoo.domain.auth.dto;

import com.hatoo.domain.groupMember.ProfileImg;
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
    private final ProfileImg profileImg;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private String fcmToken;

    public UserInfoResposne(UUID id, String status, String loginId, String email, String nickname, ProfileImg profileImg, LocalDateTime createdAt, LocalDateTime updatedAt) {
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
