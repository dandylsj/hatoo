package com.hatoo.domain.alarm;

import com.hatoo.domain.alarm.dto.NotificationHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @Operation(summary = "단건 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markRead(@Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
                                         @PathVariable UUID notificationId) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        notificationService.markRead(token, notificationId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "주간 차트 N 뱃지 조회", description = "읽지 않은 주간 차트 알림이 있으면 true를 반환합니다. 앱 실행 시 호출하여 뱃지 표시 여부를 결정합니다.")
    @GetMapping("/weekly-chart/unread")
    public ResponseEntity<Boolean> hasUnreadWeeklyChart(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return ResponseEntity.ok(notificationService.hasUnreadWeeklyChart(token));
    }

    @Operation(summary = "주간 차트 읽음 처리", description = "주간 차트 탭 진입 시 호출합니다. 해당 유저의 주간 차트 알림을 모두 읽음 처리하여 N 뱃지를 제거합니다.")
    @PatchMapping("/weekly-chart/read")
    public ResponseEntity<Void> markWeeklyChartAsRead(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        notificationService.markWeeklyChartAsRead(token);
        return ResponseEntity.ok().build();
    }
}
