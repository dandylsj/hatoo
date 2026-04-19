package com.hatoo.domain.alarm;

import com.hatoo.common.model.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Alarm", description = "FCM 알림 관련 API")
@RestController
@RequestMapping("/alarm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @Operation(summary = "FCM 토큰 저장", description = "앱 실행 시 FCM 토큰을 서버에 저장합니다.")
    @PostMapping("/token")
    public ResponseEntity<GlobalResponse<Boolean>> saveFcmToken(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestBody FcmRequest.TokenSave request) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        fcmService.saveFcmToken(token, request.getFcmToken());

        return ResponseEntity.ok(GlobalResponse.success(true));
    }

    @Operation(summary = "특정 유저에게 알림 전송", description = "특정 유저에게 FCM 푸시 알림을 전송합니다.")
    @PostMapping("/send/user")
    public ResponseEntity<GlobalResponse<Boolean>> sendToUser(
            @RequestBody FcmRequest.SendToUser request) {

        fcmService.sendToUser(request.getTargetUserId(), request.getTitle(), request.getBody());

        return ResponseEntity.ok(GlobalResponse.success(true));
    }

    @Operation(summary = "그룹 전체에 알림 전송", description = "그룹에 속한 모든 멤버에게 FCM 푸시 알림을 전송합니다.")
    @PostMapping("/send/group")
    public ResponseEntity<GlobalResponse<Boolean>> sendToGroup(
            @RequestBody FcmRequest.SendToGroup request) {

        fcmService.sendToGroup(request.getGroupId(), request.getTitle(), request.getBody());

        return ResponseEntity.ok(GlobalResponse.success(true));
    }
}
