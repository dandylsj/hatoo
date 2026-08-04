package com.hatoo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "비밀번호 재설정 인증코드 발송 요청")
public class PasswordResetSendRequest {

    @Schema(description = "로그인 아이디")
    private String loginId;

    @Schema(description = "인증코드를 받을 이메일 주소")
    private String email;
}
