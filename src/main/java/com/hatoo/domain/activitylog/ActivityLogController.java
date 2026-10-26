package com.hatoo.domain.activitylog;

import com.hatoo.domain.activitylog.dto.TaskCreationLogResponse;
import com.hatoo.domain.activitylog.dto.UserActivityResponse;
import com.hatoo.domain.activitylog.dto.UserSignupLogResponse;
import com.hatoo.domain.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// 운영 분석용 조회 API. 일반 로그인 사용자가 아닌 운영자가 X-Admin-Secret 헤더로 접근.
@Tag(name = "ActivityLog", description = "유저 가입 / 할일 등록 로그 조회 (운영자용)")
@RestController
@RequestMapping("/admin/logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final UserSignupLogRepository userSignupLogRepository;
    private final TaskCreationLogRepository taskCreationLogRepository;
    private final UserRepository userRepository;

    @Value("${admin.secret-key:hatoo-admin-secret}")
    private String secretKey;

    @Operation(summary = "유저 가입 로그 조회", description = "from/to를 생략하면 전체 기간을 최신순으로 반환합니다.")
    @GetMapping("/signups")
    public ResponseEntity<?> getSignupLogs(
            @RequestHeader("X-Admin-Secret") String requestSecret,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        if (!secretKey.equals(requestSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("잘못된 키입니다.");
        }

        List<UserSignupLog> logs = (from != null && to != null)
                ? userSignupLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to)
                : userSignupLogRepository.findAllByOrderByCreatedAtDesc();

        return ResponseEntity.ok(logs.stream().map(UserSignupLogResponse::new).collect(Collectors.toList()));
    }

    @Operation(summary = "할일 등록 로그 조회", description = "creatorId로 특정 유저만 필터링하거나, from/to로 기간을 지정할 수 있습니다.")
    @GetMapping("/tasks")
    public ResponseEntity<?> getTaskCreationLogs(
            @RequestHeader("X-Admin-Secret") String requestSecret,
            @RequestParam(required = false) UUID creatorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        if (!secretKey.equals(requestSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("잘못된 키입니다.");
        }

        List<TaskCreationLog> logs;
        if (creatorId != null) {
            logs = taskCreationLogRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId);
        } else if (from != null && to != null) {
            logs = taskCreationLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to);
        } else {
            logs = taskCreationLogRepository.findAllByOrderByCreatedAtDesc();
        }

        return ResponseEntity.ok(logs.stream().map(TaskCreationLogResponse::new).collect(Collectors.toList()));
    }

    @Operation(summary = "유저 최근 활동 시각 조회", description = "인증된 요청을 가장 최근에 보낸 순서대로 반환합니다. 아직 한 번도 활동 기록이 없는 유저는 lastActiveAt이 null입니다.")
    @GetMapping("/active-users")
    public ResponseEntity<?> getUserActivity(@RequestHeader("X-Admin-Secret") String requestSecret) {
        if (!secretKey.equals(requestSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("잘못된 키입니다.");
        }

        return ResponseEntity.ok(userRepository.findAllByOrderByLastActiveAtDesc().stream()
                .map(UserActivityResponse::new)
                .collect(Collectors.toList()));
    }
}
