package com.hatoo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "아이디 찾기 인증코드 확인 응답")
public class UserFindIdEmailCodeResponse {

    @Schema(description = "인증된 이메일로 가입된 로그인 아이디")
    private final String loginId;

    public UserFindIdEmailCodeResponse(String loginId) {
        this.loginId = loginId;
    }
}
