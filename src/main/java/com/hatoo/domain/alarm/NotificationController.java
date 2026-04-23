package com.hatoo.domain.alarm;

import com.hatoo.domain.alarm.dto.NotificationHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notification", description = "알림 내역 관련 API")
@RestController
@RequestMapping("/users/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "내 알림 목록 조회", description = "수신한 알림 목록을 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<List<NotificationHistoryResponse>> getMyNotifications(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(notificationService.getMyNotifications(token));
    }

    @Operation(summary = "전체 읽음 처리", description = "읽지 않은 알림을 모두 읽음 처리합니다. 읽음 처리된 알림은 3일 후 자동 삭제됩니다.")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        notificationService.markAllAsRead(token);
        return ResponseEntity.ok().build();
    }
}
