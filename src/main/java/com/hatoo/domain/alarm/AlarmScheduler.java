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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmScheduler {

    private final FcmService fcmService;
    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // ──────────────────────────────────────────
    // 1. 할일 시작 알림 - 매 5분마다 실행
    //    dueFrom(시작 시각)이 되면 담당자에게 전송
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void sendTaskStartAlarm() {
        List<Task> tasks = taskRepository.findByFinishedFalseAndStartAlarmSentFalse();
        LocalDateTime now = LocalDateTime.now(KST);

        tasks.forEach(task -> {
            LocalDateTime dueFromDateTime = parseDueDateTime(task.getDueFrom());
            if (dueFromDateTime == null) return;

            // dueFrom 시각이 됐고, 최대 10분 이내인 경우 발송
            if (!now.isBefore(dueFromDateTime) && now.isBefore(dueFromDateTime.plusMinutes(10))) {
                if (!task.getAssignees().isEmpty()) {
                    UUID userId = task.getAssignees().get(0).getId();
                    fcmService.sendTaskStart(userId, task.getTitle());
                    task.markStartAlarmSent();
                    log.info("[AlarmScheduler] 할일 시작 알림 발송 - taskId: {}, dueFrom: {}", task.getId(), task.getDueFrom());
                }
            }
        });
    }

    // ──────────────────────────────────────────
    // 2. 마감 임박 알림 - 매 5분마다 실행
    //    dueTo(마감 시각) 기준, deadLine 컬럼에 설정된 시간만큼 앞서서 알림
    //    예) deadLine=DAY_1  → dueTo 하루 전에 전송
    //        deadLine=MIN_30 → dueTo 30분 전에 전송
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void sendTaskDeadlineAlarm() {
        List<Task> tasks = taskRepository.findTasksForDeadlineAlarm();
        LocalDateTime now = LocalDateTime.now(KST);

        tasks.forEach(task -> {
            LocalDateTime dueToDateTime = parseDueDateTime(task.getDueTo());
            if (dueToDateTime == null) return;

            Duration duration = getDeadLineDuration(task.getDeadLine());
            if (duration == null) return;

            // 알림 발송 시각 = dueTo - deadLine 기간
            LocalDateTime notifyAt = dueToDateTime.minus(duration);

            // notifyAt이 됐고 최대 10분 이내인 경우 발송
            if (!now.isBefore(notifyAt) && now.isBefore(notifyAt.plusMinutes(10))) {
                if (!task.getAssignees().isEmpty()) {
                    UUID userId = task.getAssignees().get(0).getId();
                    fcmService.sendTaskDeadline(userId, task.getTitle());
                    task.markDeadlineAlarmSent();
                    log.info("[AlarmScheduler] 마감 임박 알림 발송 - taskId: {}, deadLine: {}, dueTo: {}", task.getId(), task.getDeadLine(), task.getDueTo());
                }
            }
        });
    }

    // ──────────────────────────────────────────
    // 3. 마감 초과 알림 - 매 10분마다 실행
    //    dueTo(마감 시각)로부터 2시간이 지났는데도 미완료인 할일 담당자에게 전송
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void sendTaskOverdueAlarm() {
        List<Task> tasks = taskRepository.findByFinishedFalseAndOverdueAlarmSentFalse();
        LocalDateTime now = LocalDateTime.now(KST);

        tasks.forEach(task -> {
            LocalDateTime dueToDateTime = parseDueDateTime(task.getDueTo());
            if (dueToDateTime == null) return;

            // 마감 초과 기준 시각 = dueTo + 2시간
            LocalDateTime overdueAt = dueToDateTime.plusHours(2);

            // overdueAt이 됐고 최대 15분 이내인 경우 발송
            if (!now.isBefore(overdueAt) && now.isBefore(overdueAt.plusMinutes(15))) {
                if (!task.getAssignees().isEmpty()) {
                    UUID userId = task.getAssignees().get(0).getId();
                    fcmService.sendTaskOverdue(userId, task.getTitle());
                    task.markOverdueAlarmSent();
                    log.info("[AlarmScheduler] 마감 초과 알림 발송 - taskId: {}, dueTo: {}", task.getId(), task.getDueTo());
                }
            }
        });
    }

    // ──────────────────────────────────────────
    // 4. 주간 차트 공개 알림 - 매주 월요일 오전 8시
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 0 8 * * MON")
    public void sendWeeklyChartAlarm() {
        log.info("[AlarmScheduler] 주간 차트 공개 알림 전송");
        List<Group> groups = groupRepository.findAll();
        groups.forEach(group -> fcmService.sendWeeklyChart(group.getId()));
    }

    // ──────────────────────────────────────────
    // 8. 비활성 그룹 알림 - 매월 1일 오전 10시
    //    최근 30일간 할일 업데이트가 없는 그룹에 전송
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 0 10 1 * *")
    public void sendInactiveGroupAlarm() {
        log.info("[AlarmScheduler] 비활성 그룹 알림 전송");
        LocalDateTime thirtyDaysAgo = LocalDateTime.now(KST).minusDays(30);
        List<Group> inactiveGroups = groupRepository.findInactiveGroups(thirtyDaysAgo);
        inactiveGroups.forEach(group -> fcmService.sendInactiveGroup(group.getId()));
    }

    // ──────────────────────────────────────────
    // 내부 유틸 메서드
    // ──────────────────────────────────────────

    /**
     * dueFrom/dueTo 문자열을 KST 기준 LocalDateTime으로 파싱
     * - "2026-04-19T13:00:00.000Z" (UTC, Z suffix) → KST +9시간으로 변환
     * - "2026-04-19T22:00:00"      (로컬 시각, Z 없음) → 그대로 사용
     * - "2026-04-19"               (날짜만)            → 해당 날 00:00:00 KST
     */
    private LocalDateTime parseDueDateTime(String due) {
        if (due == null || due.isBlank()) return null;
        try {
            String trimmed = due.trim();
            // UTC 형식 (Z suffix) → KST로 변환
            if (trimmed.endsWith("Z")) {
                return Instant.parse(trimmed)
                        .atZone(KST)
                        .toLocalDateTime();
            }
            // 로컬 datetime 형식 (시간 포함)
            if (trimmed.length() >= 19) {
                String normalized = trimmed.substring(0, 19).replace(' ', 'T');
                return LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            // 날짜만 있는 경우
            return LocalDate.parse(trimmed.substring(0, 10)).atStartOfDay();
        } catch (Exception e) {
            log.warn("[AlarmScheduler] 날짜 파싱 실패 - value: {}", due);
            return null;
        }
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
