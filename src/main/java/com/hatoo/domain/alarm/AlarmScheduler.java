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
    // 1. 할일 시작 알림 - 매 5분마다 실행
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void sendTaskStartAlarm() {
        List<Task> tasks = taskRepository.findByFinishedFalseAndStartAlarmSentFalse();
        LocalDateTime now = LocalDateTime.now(KST);

        tasks.forEach(task -> {
            LocalDateTime dueFromDateTime = parseDueDateTime(task.getDueFrom());
            if (dueFromDateTime == null) return;

            if (!now.isBefore(dueFromDateTime) && now.isBefore(dueFromDateTime.plusMinutes(10))) {
                if (!task.getAssignees().isEmpty()) {
                    UUID userId = task.getAssignees().get(0).getId();
                    fcmService.sendTaskStart(userId, task.getTitle());
                    task.markStartAlarmSent();
                    log.info("[AlarmScheduler] 할일 시작 알림 발송 - taskId: {}", task.getId());
                }
            }
        });
    }

    // ──────────────────────────────────────────
    // 2. 마감 임박 알림 - 매 5분마다 실행
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

            LocalDateTime notifyAt = dueToDateTime.minus(duration);

            if (!now.isBefore(notifyAt) && now.isBefore(notifyAt.plusMinutes(10))) {
                if (!task.getAssignees().isEmpty()) {
                    UUID userId = task.getAssignees().get(0).getId();
                    fcmService.sendTaskDeadline(userId, task.getTitle());
                    task.markDeadlineAlarmSent();
                    log.info("[AlarmScheduler] 마감 임박 알림 발송 - taskId: {}", task.getId());
                }
            }
        });
    }

    // ──────────────────────────────────────────
    // 3. 마감 초과 알림 - 매 10분마다 실행
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void sendTaskOverdueAlarm() {
        List<Task> tasks = taskRepository.findByFinishedFalseAndOverdueAlarmSentFalse();
        LocalDateTime now = LocalDateTime.now(KST);

        tasks.forEach(task -> {
            LocalDateTime dueToDateTime = parseDueDateTime(task.getDueTo());
            if (dueToDateTime == null) return;

            LocalDateTime overdueAt = dueToDateTime.plusHours(2);

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
    // 4. 주간 차트 알림 - 매주 월요일 오전 8시
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 0 8 * * MON", zone = "Asia/Seoul")
    @Transactional
    public void sendWeeklyChartAlarm() {
        List<Group> groups = groupRepository.findAll();
        groups.forEach(group -> {
            fcmService.sendWeeklyChart(group.getId());
            log.info("[AlarmScheduler] 주간 차트 알림 발송 - groupId: {}", group.getId());
        });
    }

    // ──────────────────────────────────────────
    // 5. 비활성 그룹 알림 - 매월 1일 오전 9시
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 0 9 1 * *", zone = "Asia/Seoul")
    @Transactional
    public void sendInactiveGroupAlarm() {
        LocalDate cutoff = LocalDate.now(KST).minusDays(30);
        List<Group> groups = groupRepository.findAll();

        groups.forEach(group -> {
            boolean hasRecentTask = taskRepository
                    .findByGroupsId(group.getId())
                    .stream()
                    .anyMatch(t -> t.getCreatedAt() != null &&
                            t.getCreatedAt().toLocalDate().isAfter(cutoff));

            if (!hasRecentTask) {
                fcmService.sendInactiveGroup(group.getId());
                log.info("[AlarmScheduler] 비활성 그룹 알림 발송 - groupId: {}", group.getId());
            }
        });
    }

    // ──────────────────────────────────────────
    // 6. 읽음 처리된 알림 삭제 - 매일 자정 (00:00 KST)
    //    읽음 처리 후 3일이 지난 알림 자동 삭제
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deleteOldReadNotifications() {
        LocalDateTime cutoff = LocalDateTime.now(KST).minusDays(3);
        notificationHistoryRepository.deleteReadNotificationsOlderThan(cutoff);
        log.info("[AlarmScheduler] 읽음 처리 3일 경과 알림 삭제 완료 - 기준시각: {}", cutoff);
    }

    // ──────────────────────────────────────────
    // 유틸 메서드
    // ──────────────────────────────────────────
    private LocalDateTime parseDueDateTime(String due) {
        if (due == null || due.isBlank()) return null;
        try {
            return LocalDateTime.parse(due, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
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
