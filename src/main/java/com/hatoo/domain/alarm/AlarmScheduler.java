package com.hatoo.domain.alarm;

import com.hatoo.domain.groups.Group;
import com.hatoo.domain.groups.GroupRepository;
import com.hatoo.domain.task.DeadLine;
import com.hatoo.domain.task.Task;
import com.hatoo.domain.task.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmScheduler {

    private final FcmService fcmService;
    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // ──────────────────────────────────────────
    // 1. 할일 시작 알림 - 매 분 정각 실행
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    @Transactional
    public void sendTaskStartAlarm() {
        List<Task> tasks = taskRepository.findByStarterTrueAndFinishedFalseAndStartAlarmSentFalse();
        LocalDateTime nowMinute = LocalDateTime.now(KST).truncatedTo(ChronoUnit.MINUTES);

        tasks.forEach(task -> {
            LocalDateTime dueFromDateTime = parseDueDateTime(task.getDueFrom());
            if (dueFromDateTime == null) return;

            // dueFrom이 현재 분(分) 안에 있을 때만 전송 (예: 13:52:xx → 13:52:00 정각에 발송)
            LocalDateTime dueFromMinute = dueFromDateTime.truncatedTo(ChronoUnit.MINUTES);
            if (dueFromMinute.equals(nowMinute)) {
                if (!task.getAssignees().isEmpty()) {
                    Group taskGroup = task.getGroups().isEmpty() ? null : task.getGroups().get(0);
                    UUID groupId = taskGroup != null ? taskGroup.getId() : null;
                    String groupName = taskGroup != null ? taskGroup.getName() : null;
                    task.getAssignees().forEach(assignee ->
                            fcmService.sendTaskStart(assignee.getId(), task.getTitle(), task.getId(), groupId, groupName)
                    );
                    task.markStartAlarmSent();
                    log.info("[AlarmScheduler] 할일 시작 알림 발송 - taskId: {}", task.getId());
                }
            }
        });
    }

    // ──────────────────────────────────────────
    // 2. 마감 임박 알림 - 매 분 정각 실행
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    @Transactional
    public void sendTaskDeadlineAlarm() {
        List<Task> tasks = taskRepository.findTasksForDeadlineAlarm();
        LocalDateTime nowMinute = LocalDateTime.now(KST).truncatedTo(ChronoUnit.MINUTES);

        tasks.forEach(task -> {
            LocalDateTime dueToDateTime = parseDueDateTime(task.getDueTo());
            if (dueToDateTime == null) return;

            Duration duration = getDeadLineDuration(task.getDeadLine());
            if (duration == null) return;

            LocalDateTime notifyAt = dueToDateTime.minus(duration).truncatedTo(ChronoUnit.MINUTES);

            if (notifyAt.equals(nowMinute)) {
                if (!task.getAssignees().isEmpty()) {
                    Group taskGroup = task.getGroups().isEmpty() ? null : task.getGroups().get(0);
                    UUID groupId = taskGroup != null ? taskGroup.getId() : null;
                    String groupName = taskGroup != null ? taskGroup.getName() : null;
                    task.getAssignees().forEach(assignee ->
                            fcmService.sendTaskDeadline(assignee.getId(), task.getTitle(), task.getId(), groupId, groupName)
                    );
                    task.markDeadlineAlarmSent();
                    log.info("[AlarmScheduler] 마감 임박 알림 발송 - taskId: {}", task.getId());
                }
            }
        });
    }

    // ──────────────────────────────────────────
    // 3. 마감 초과 알림 - 매 분 정각 실행
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    @Transactional
    public void sendTaskOverdueAlarm() {
        List<Task> tasks = taskRepository.findByFinishedFalseAndOverdueAlarmSentFalse();
        LocalDateTime nowMinute = LocalDateTime.now(KST).truncatedTo(ChronoUnit.MINUTES);

        tasks.forEach(task -> {
            LocalDateTime dueToDateTime = parseDueDateTime(task.getDueTo());
            if (dueToDateTime == null) return;

            LocalDateTime overdueAt = dueToDateTime.plusHours(2).truncatedTo(ChronoUnit.MINUTES);

            if (overdueAt.equals(nowMinute)) {
                if (!task.getAssignees().isEmpty()) {
                    Group taskGroup = task.getGroups().isEmpty() ? null : task.getGroups().get(0);
                    UUID groupId = taskGroup != null ? taskGroup.getId() : null;
                    String groupName = taskGroup != null ? taskGroup.getName() : null;
                    task.getAssignees().forEach(assignee ->
                            fcmService.sendTaskOverdue(assignee.getId(), task.getTitle(), task.getId(), groupId, groupName)
                    );
                    task.markOverdueAlarmSent();
                    log.info("[AlarmScheduler] 마감 초과 알림 발송 - taskId: {}", task.getId());
                }
            }
        });
    }

    // ──────────────────────────────────────────
    // 4. 주간 차트 알림 - 매주 월요일 오전 8시
    //    여러 그룹에 속한 유저도 알림은 1번만 수신
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 0 8 * * MON", zone = "Asia/Seoul")
    @Transactional
    public void sendWeeklyChartAlarm() {
        List<Group> groups = groupRepository.findAll();

        // 전체 그룹에서 유저 UUID를 수집하되 중복 제거
        java.util.Set<UUID> notifiedUsers = new java.util.HashSet<>();

        groups.forEach(group -> {
            fcmService.sendWeeklyChart(group.getId(), group.getName(), notifiedUsers);
            log.info("[AlarmScheduler] 주간 차트 알림 발송 - groupId: {}", group.getId());
        });
    }

    // ──────────────────────────────────────────
    // 5. 읽음 처리된 알림 삭제 - 매일 자정 (00:00 KST)
    //    읽음 처리 후 7일이 지난 알림 자동 삭제
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deleteOldReadNotifications() {
        LocalDateTime cutoff = LocalDateTime.now(KST).minusDays(7);
        notificationHistoryRepository.deleteReadNotificationsOlderThan(cutoff);
        log.info("[AlarmScheduler] 읽음 처리 7일 경과 알림 삭제 완료 - 기준시각: {}", cutoff);
    }

    // ──────────────────────────────────────────
    // 유틸 메서드
    // ──────────────────────────────────────────
    private LocalDateTime parseDueDateTime(String due) {
        if (due == null || due.isBlank()) return null;

        // 1. ISO 8601 형식 "2026-04-27T01:56:04.689Z" (UTC → KST 변환)
        if (due.contains("T")) {
            try {
                java.time.Instant instant = java.time.Instant.parse(due);
                return LocalDateTime.ofInstant(instant, KST);
            } catch (Exception ignored) {}
            // Z 없는 ISO 형식 "2026-04-27T13:55:01"
            try {
                return LocalDateTime.parse(due, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception ignored) {}
        }

        // 2. 공백 구분 형식 "2026-04-28 13:55:01.884884" 등
        String[] patterns = {
            "yyyy-MM-dd HH:mm:ss.SSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm"
        };

        for (String pattern : patterns) {
            try {
                String trimmed = due.length() > pattern.length() ? due.substring(0, pattern.length()) : due;
                return LocalDateTime.parse(trimmed, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {}
        }

        // 3. 날짜만 "2026-04-28" → 자정 00:00
        try {
            return LocalDate.parse(due.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        } catch (Exception ignored) {}

        log.warn("[AlarmScheduler] 날짜 파싱 실패 - due: {}", due);
        return null;
    }

    /**
     * DeadLine 열거형을 Duration으로 변환
     */
    private Duration getDeadLineDuration(DeadLine deadLine) {
        if (deadLine == null) return null;
        return switch (deadLine) {
            case MIN_10 -> Duration.ofMinutes(10);
            case MIN_30 -> Duration.ofMinutes(30);
            case HOUR_1 -> Duration.ofHours(1);
            case DAY_1  -> Duration.ofDays(1);
            case WEEK_1 -> Duration.ofDays(7);
            case NONE   -> null;
        };
    }
}