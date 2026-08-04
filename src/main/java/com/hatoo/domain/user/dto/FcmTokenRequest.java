package com.hatoo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "FCM 푸시 토큰 등록 요청")
public class FcmTokenRequest {

    @Schema(description = "FCM 디바이스 토큰")
    private String fcmToken;

    @Schema(description = "디바이스 종류 (ANDROID 또는 IOS)")
    private String deviceType;
}
