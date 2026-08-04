package com.hatoo.domain.oAuth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "구글 로그인 요청")
public class GoogleLoginRequest {

    @Schema(description = "구글 ID 토큰")
    private String idToken;
}
