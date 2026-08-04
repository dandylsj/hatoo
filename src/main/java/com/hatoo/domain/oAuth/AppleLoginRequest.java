package com.hatoo.domain.oAuth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "애플 로그인 요청")
public class AppleLoginRequest {

    @Schema(description = "iOS에서 Apple 로그인 후 받은 JWT 토큰 (필수)")
    private String identityToken;

    @Schema(description = "사용자 이름 (iOS 최초 로그인 시에만 전달됨, 이후 로그인에서는 null)")
    private String nickname;
}
