package com.hatoo.domain.oAuth;

import lombok.Getter;

@Getter
public class AppleLoginRequest {

    // iOS에서 Apple 로그인 후 받은 JWT 토큰 (필수)
    private String identityToken;

    // 사용자 이름 - iOS 최초 로그인 시에만 전달됨, 이후 로그인에서는 null
    private String nickname;
}
