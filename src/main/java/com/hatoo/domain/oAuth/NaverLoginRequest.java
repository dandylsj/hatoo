package com.hatoo.domain.oAuth;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverLoginRequest {

    private String accessToken;           // 네이버 액세스 토큰
    private String refreshToken;   // 네이버 리프레시 토큰 (탈퇴 시 연결 해제용)

}
