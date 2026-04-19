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

    // ──────────────────────────────────────────
    // 1. 할일 시작 알림 - 매 5분마다 실행
    //    dueTo 에 설정된 시각이 되면 담당자에게 전송
    //    예) dueTo = "2024-08-10T14:00:00" → 14:00에 알림 발송
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void sendTaskStartAlarm() {
        List<Task> tasks = taskRepository.findByFinishedFalseAndStartAlarmSentFalse();
        LocalDateTime now = LocalDateTime.now();

        tasks.forEach(task -> {
            LocalDateTime dueToDateTime = parseDueDateTime(task.getDueTo());
            if (dueToDateTime == null) return;

            // dueTo 시각이 지났고, 최대 10분 이내인 경우 발송
            if (!now.isBefore(dueToDateTime) && now.isBefore(dueToDateTime.plusMinutes(10))) {
                if (!task.getAssignees().isEmpty()) {
                    UUID userId = task.getAssignees().get(0).getId();
                    fcmService.sendTaskStart(userId, task.getTitle());
                    task.markStartAlarmSent();
                    log.info("[AlarmScheduler] 할일 시작 알림 발송 - taskId: {}, dueTo: {}", task.getId(), task.getDueTo());
                }
            }
        });
    }

    // ──────────────────────────────────────────
    // 2. 마감 임박 알림 - 매 5분마다 실행
    //    dueFrom 기준, deadLine 컬럼에 설정된 시간만큼 앞서서 알림
    //    예) deadLine=DAY_1 이면 dueFrom 하루 전에 전송
    //        deadLine=MIN_30 이면 dueFrom 30분 전에 전송
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void sendTaskDeadlineAlarm() {
        List<Task> tasks = taskRepository.findTasksForDeadlineAlarm();
        LocalDateTime now = LocalDateTime.now();

        tasks.forEach(task -> {
            LocalDateTime dueFromDateTime = parseDueDateTime(task.getDueFrom());
            if (dueFromDateTime == null) return;

            Duration duration = getDeadLineDuration(task.getDeadLine());
            if (duration == null) return;

            // 알림 발송 시각 = dueFrom - deadLine 기간
            LocalDateTime notifyAt = dueFromDateTime.minus(duration);

            // notifyAt이 이미 지났고 최대 10분 이내인 경우 발송
            if (!now.isBefore(notifyAt) && now.isBefore(notifyAt.plusMinutes(10))) {
                if (!task.getAssignees().isEmpty()) {
                    UUID userId = task.getAssignees().get(0).getId();
                    fcmService.sendTaskDeadline(userId, task.getTitle());
                    task.markDeadlineAlarmSent();
                    log.info("[AlarmScheduler] 마감 임박 알림 발송 - taskId: {}, deadLine: {}", task.getId(), task.getDeadLine());
                }
            }
        });
    }

    // ──────────────────────────────────────────
    // 3. 마감 초과 알림 - 매 10분마다 실행
    //    dueFrom 으로부터 2시간이 지났는데도 미완료인 할일 담당자에게 전송
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void sendTaskOverdueAlarm() {
        List<Task> tasks = taskRepository.findByFinishedFalseAndOverdueAlarmSentFalse();
        LocalDateTime now = LocalDateTime.now();

        tasks.forEach(task -> {
            LocalDateTime dueFromDateTime = parseDueDateTime(task.getDueFrom());
            if (dueFromDateTime == null) return;

            // 마감 초과 기준 시각 = dueFrom + 2시간
            LocalDateTime overdueAt = dueFromDateTime.plusHours(2);

            // overdueAt이 이미 지났고 최대 15분 이내인 경우 발송
            if (!now.isBefore(overdueAt) && now.isBefore(overdueAt.plusMinutes(15))) {
                if (!task.getAssignees().isEmpty()) {
                    UUID userId = task.getAssignees().get(0).getId();
                    fcmService.sendTaskOverdue(userId, task.getTitle());
                    task.markOverdueAlarmSent();
                    log.info("[AlarmScheduler] 마감 초과 알림 발송 - taskId: {}", task.getId());
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

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Group> inactiveGroups = groupRepository.findInactiveGroups(thirtyDaysAgo);
        inactiveGroups.forEach(group -> fcmService.sendInactiveGroup(group.getId()));
    }

    // ──────────────────────────────────────────
    // 내부 유틸 메서드
    // ──────────────────────────────────────────

    /**
     * dueFrom/dueTo 문자열을 LocalDateTime으로 파싱
     * - "2024-08-10T09:00:00" 또는 "2024-08-10 09:00:00" → 해당 시각
     * - "2024-08-10" (날짜만) → 해당 날 00:00:00
     */
    private LocalDateTime parseDueDateTime(String due) {
        if (due == null || due.isBlank()) return null;
        try {
            String trimmed = due.trim();
            if (trimmed.length() >= 19) {
                String normalized = trimmed.substring(0, 19).replace(' ', 'T');
                return LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else {
                return LocalDate.parse(trimmed.substring(0, 10)).atStartOfDay();
            }
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
