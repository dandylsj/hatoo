package com.hatoo.domain.activitylog.dto;

import com.hatoo.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Schema(description = "유저 최근 활동 시각 응답")
public class UserActivityResponse {

    @Schema(description = "유저 ID")
    private final UUID userId;

    @Schema(description = "닉네임")
    private final String nickname;

    @Schema(description = "이메일")
    private final String email;

    @Schema(description = "최근 활동(인증된 요청) 시각. 한 번도 없으면 null")
    private final LocalDateTime lastActiveAt;

    public UserActivityResponse(User user) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.lastActiveAt = user.getLastActiveAt();
    }
}
