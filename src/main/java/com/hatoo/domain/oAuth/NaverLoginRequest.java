package com.hatoo.domain.oAuth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "네이버 로그인 요청")
public class NaverLoginRequest {

    @Schema(description = "네이버 액세스 토큰")
    private String accessToken;

    @Schema(description = "네이버 리프레시 토큰 (탈퇴 시 연결 해제용)")
    private String refreshToken;

}
