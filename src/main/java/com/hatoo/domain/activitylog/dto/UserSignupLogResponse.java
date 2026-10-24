package com.hatoo.domain.activitylog.dto;

import com.hatoo.domain.activitylog.SignupProvider;
import com.hatoo.domain.activitylog.UserSignupLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Schema(description = "유저 가입 로그 응답")
public class UserSignupLogResponse {

    @Schema(description = "유저 ID")
    private final UUID userId;

    @Schema(description = "이메일")
    private final String email;

    @Schema(description = "닉네임")
    private final String nickname;

    @Schema(description = "가입 경로")
    private final SignupProvider provider;

    @Schema(description = "가입 시각")
    private final LocalDateTime createdAt;

    public UserSignupLogResponse(UserSignupLog log) {
        this.userId = log.getUserId();
        this.email = log.getEmail();
        this.nickname = log.getNickname();
        this.provider = log.getProvider();
        this.createdAt = log.getCreatedAt();
    }
}
