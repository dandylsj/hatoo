package com.hatoo.domain.alarm;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/alarm")
@RequiredArgsConstructor
public class TestAlarmController {

    private final AlarmScheduler alarmScheduler;

    @Value("${test.alarm.secret-key:hatoo-test-secret}")
    private String secretKey;

    @PostMapping("/weekly")
    public ResponseEntity<String> triggerWeeklyAlarm(
            @RequestHeader("X-Test-Secret") String requestSecret) {
        if (!secretKey.equals(requestSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("잘못된 키입니다.");
        }
        alarmScheduler.sendWeeklyChartAlarm();
        return ResponseEntity.ok("주간 차트 알림 전송 완료");
    }

    @PostMapping("/task-start")
    public ResponseEntity<String> triggerTaskStartAlarm(
            @RequestHeader("X-Test-Secret") String requestSecret) {
        if (!secretKey.equals(requestSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("잘못된 키입니다.");
        }
        alarmScheduler.sendTaskStartAlarm();
        return ResponseEntity.ok("할일 시작 알림 전송 완료");
    }
}
