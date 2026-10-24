package com.hatoo.domain.alarm;

import com.hatoo.domain.groups.Group;
import com.hatoo.domain.groups.GroupRepository;
import com.hatoo.domain.task.Task;
import com.hatoo.domain.task.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    //    알림 시각(start_alarm_at)이 이번 1분 안에 있는 할일만 인덱스로 조회한다.
    //    (예: 13:52:xx 시작 할일 → 13:52:00 정각 실행분에서 발송)
    //    예전에는 미완료 할일 전체를 불러와 dueFrom 문자열을 파싱해서 걸렀다 - TaskAlarmTimes 참고.
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    @Transactional
    public void sendTaskStartAlarm() {
        sendTaskStartAlarm(currentMinute());
    }

    @Transactional
    public void sendTaskStartAlarm(LocalDateTime nowMinute) {
        taskRepository.findStartAlarmTargets(nowMinute, nowMinute.plusMinutes(1)).forEach(task -> {
            if (notifyAssignees(task, fcmService::sendTaskStart)) {
                task.markStartAlarmSent();
                log.info("[AlarmScheduler] 할일 시작 알림 발송 - taskId: {}", task.getId());
            }
        });
    }

    // ──────────────────────────────────────────
    // 2. 마감 임박 알림 - 매 분 정각 실행 (알림 시각 = 마감 - 마감 임박 설정)
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    @Transactional
    public void sendTaskDeadlineAlarm() {
        sendTaskDeadlineAlarm(currentMinute());
    }

    @Transactional
    public void sendTaskDeadlineAlarm(LocalDateTime nowMinute) {
        taskRepository.findDeadlineAlarmTargets(nowMinute, nowMinute.plusMinutes(1)).forEach(task -> {
            if (notifyAssignees(task, fcmService::sendTaskDeadline)) {
                task.markDeadlineAlarmSent();
                log.info("[AlarmScheduler] 마감 임박 알림 발송 - taskId: {}", task.getId());
            }
        });
    }

    // ──────────────────────────────────────────
    // 3. 마감 초과 알림 - 매 분 정각 실행 (알림 시각 = 마감 + 2시간)
    // ──────────────────────────────────────────
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    @Transactional
    public void sendTaskOverdueAlarm() {
        sendTaskOverdueAlarm(currentMinute());
    }

    @Transactional
    public void sendTaskOverdueAlarm(LocalDateTime nowMinute) {
        taskRepository.findOverdueAlarmTargets(nowMinute, nowMinute.plusMinutes(1)).forEach(task -> {
            if (notifyAssignees(task, fcmService::sendTaskOverdue)) {
                task.markOverdueAlarmSent();
                log.info("[AlarmScheduler] 마감 초과 알림 발송 - taskId: {}", task.getId());
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

        groups.stream()
                .filter(group -> !group.isPersonal())
                .forEach(group -> {
                    fcmService.sendWeeklyChart(group.getId(), group.getName());
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
    private LocalDateTime currentMinute() {
        return LocalDateTime.now(KST).truncatedTo(ChronoUnit.MINUTES);
    }

    /** 담당자 전원에게 알림을 보낸다. 담당자가 없으면 보내지 않고 false (발송 플래그도 세우지 않음 - 기존 동작 유지). */
    private boolean notifyAssignees(Task task, TaskAlarmSender sender) {
        if (task.getAssignees().isEmpty()) return false;
        Group taskGroup = task.getGroups().isEmpty() ? null : task.getGroups().get(0);
        UUID groupId = taskGroup != null ? taskGroup.getId() : null;
        String groupName = taskGroup != null ? taskGroup.getName() : null;
        task.getAssignees().forEach(assignee ->
                sender.send(assignee.getId(), task.getTitle(), task.getId(), groupId, groupName));
        return true;
    }

    @FunctionalInterface
    private interface TaskAlarmSender {
        void send(UUID userId, String taskTitle, UUID taskId, UUID groupId, String groupName);
    }
}
