package com.hatoo.domain.alarm;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class FcmRequest {

    // FCM 토큰 저장 요청
    @Getter
    @NoArgsConstructor
    public static class TokenSave {
        private String fcmToken;
    }

    // 단일 유저에게 알람 전송
    @Getter
    @NoArgsConstructor
    public static class SendToUser {
        private UUID targetUserId;
        private String title;
        private String body;
    }

    // 그룹 전체에 알람 전송
    @Getter
    @NoArgsConstructor
    public static class SendToGroup {
        private UUID groupId;
        private String title;
        private String body;
    }
}
