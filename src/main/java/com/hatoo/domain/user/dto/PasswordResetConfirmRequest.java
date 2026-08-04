package com.hatoo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "비밀번호 재설정 확인 요청")
public class PasswordResetConfirmRequest {

    @Schema(description = "재설정 인증코드를 받은 이메일 주소")
    private String email;

    @Schema(description = "이메일로 받은 인증코드")
    private String token;

    @Schema(description = "새 비밀번호")
    private String newPassword;
}
