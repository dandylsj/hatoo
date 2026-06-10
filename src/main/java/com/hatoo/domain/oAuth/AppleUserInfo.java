package com.hatoo.domain.oAuth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppleUserInfo {

    // Apple 고유 사용자 식별자 (JWT의 sub 클레임)
    private String sub;

    // 사용자 이메일 (공개 동의 시 실제 이메일, 비공개 선택 시 릴레이 이메일 또는 null)
    private String email;
}
