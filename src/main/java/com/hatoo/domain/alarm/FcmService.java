package com.hatoo.domain.alarm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.alarmUserAgree.AlarmUserAgree;
import com.hatoo.domain.alarmUserAgree.AlarmUserAgreeRepository;
import com.hatoo.domain.groupAlarmSetting.GroupAlarmSetting;
import com.hatoo.domain.groupAlarmSetting.GroupAlarmSettingRepository;
import com.hatoo.domain.groupMember.GroupMember;
import com.hatoo.domain.groupMember.GroupMemberRepository;
import com.hatoo.domain.user.User;
import com.hatoo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final AlarmUserAgreeRepository alarmUserAgreeRepository;
    private final GroupAlarmSettingRepository groupAlarmSettingRepository;
    private final JwtUtil jwtUtil;

    // ──────────────────────────────────────────
    // 1. 할일 시작 알림 (스케줄러 호출)
    // ──────────────────────────────────────────

    public void sendTaskStart(UUID userId, String taskTitle) {
        String body = String.format(AlarmType.TASK_START.getBodyTemplate(), taskTitle);
        sendToUserIfAllowed(userId, AlarmType.TASK_START.getTitle(), body);
    }

    // ──────────────────────────────────────────
    // 2. 할일 마감 임박 알림 (스케줄러 호출)
    // ──────────────────────────────────────────

    public void sendTaskDeadline(UUID userId, String taskTitle) {
        String body = String.format(AlarmType.TASK_DEADLINE.getBodyTemplate(), taskTitle);
        sendToUserIfAllowed(userId, AlarmType.TASK_DEADLINE.getTitle(), body);
    }

    // ──────────────────────────────────────────
    // 3. 마감 초과 알림 (스케줄러 호출)
    // ──────────────────────────────────────────

    public void sendTaskOverdue(UUID userId, String taskTitle) {
        String body = String.format(AlarmType.TASK_OVERDUE.getBodyTemplate(), taskTitle);
        sendToUserIfAllowed(userId, AlarmType.TASK_OVERDUE.getTitle(), body);
    }

    // ──────────────────────────────────────────
    // 4. 주간 차트 공개 알림 (매주 월요일 8시 스케줄러 호출)
    // ──────────────────────────────────────────

    public void sendWeeklyChart(UUID groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.forEach(gm ->
                sendToUserIfAllowed(gm.getUser().getId(),
                        AlarmType.WEEKLY_CHART.getTitle(),
                        AlarmType.WEEKLY_CHART.getBodyTemplate())
        );
    }

    // ──────────────────────────────────────────
    // 5. 새 멤버 참여 알림 (GroupService에서 호출)
    // ──────────────────────────────────────────

    public void sendNewMember(UUID groupId, String groupName, String newMemberNickname) {
        String body = String.format(AlarmType.NEW_MEMBER.getBodyTemplate(), groupName, newMemberNickname);
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.forEach(gm ->
                sendToGroupMemberIfAllowed(gm.getUser().getId(), groupId, "newMember",
                        AlarmType.NEW_MEMBER.getTitle(), body)
        );
    }

    // ──────────────────────────────────────────
    // 6. 새 집안일 등록 알림 (TaskService에서 호출)
    // ──────────────────────────────────────────

    public void sendTaskCreated(UUID groupId, String creatorNickname, String taskTitle) {
        String body = String.format(AlarmType.TASK_CREATED.getBodyTemplate(), creatorNickname, taskTitle);
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.forEach(gm ->
                sendToGroupMemberIfAllowed(gm.getUser().getId(), groupId, "newTask",
                        AlarmType.TASK_CREATED.getTitle(), body)
        );
    }

    // ──────────────────────────────────────────
    // 7. 집안일 배정 알림 (TaskService에서 호출) - 개인 알림, 그룹 설정 미적용
    // ──────────────────────────────────────────

    public void sendTaskAssigned(UUID assigneeId, String assignerNickname, String assigneeNickname) {
        String body = String.format(AlarmType.TASK_ASSIGNED.getBodyTemplate(), assignerNickname, assigneeNickname);
        sendToUserIfAllowed(assigneeId, AlarmType.TASK_ASSIGNED.getTitle(), body);
    }

    // ──────────────────────────────────────────
    // 8. 비활성 그룹 알림 (월간 스케줄러 호출)
    // ──────────────────────────────────────────

    public void sendInactiveGroup(UUID groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.forEach(gm ->
                sendToGroupMemberIfAllowed(gm.getUser().getId(), groupId, "general",
                        AlarmType.INACTIVE_GROUP.getTitle(),
                        AlarmType.INACTIVE_GROUP.getBodyTemplate())
        );
    }

    // ──────────────────────────────────────────
    // 9. 집안일 완료 알림 (TaskService에서 호출)
    // ──────────────────────────────────────────

    public void sendTaskComplete(UUID groupId, String finisherNickname, String taskTitle) {
        String body = String.format("%s님이 [%s]을(를) 완료했어요!", finisherNickname, taskTitle);
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.forEach(gm ->
                sendToGroupMemberIfAllowed(gm.getUser().getId(), groupId, "taskComplete",
                        "집안일 완료", body)
        );
    }

    // ──────────────────────────────────────────
    // 내부 공통 메서드
    // ──────────────────────────────────────────

    // 개인 알림: 전체 알림 마스터 + 개인 알림 토글 확인
    private void sendToUserIfAllowed(UUID userId, String title, String body) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        AlarmUserAgree agree = alarmUserAgreeRepository.findByUserId(userId).orElse(null);

        // 1단계: 전체 알림 마스터
        if (agree != null && !Boolean.TRUE.equals(agree.getIsAllNotiEnabled())) {
            log.info("[FCM] 전체 알림 OFF - userId: {}", userId);
            return;
        }
        // 2단계: 개인 알림 토글
        if (agree != null && !Boolean.TRUE.equals(agree.getIsPersonalNotiEnabled())) {
            log.info("[FCM] 개인 알림 OFF - userId: {}", userId);
            return;
        }

        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            log.warn("[FCM] FCM 토큰 없음 - userId: {}", userId);
            return;
        }

        sendMessage(user.getFcmToken(), title, body);
    }

    // 그룹 알림: 전체 마스터 → 그룹 알림 전체 → 그룹별 마스터 → 세부 설정 4단계 확인
    private void sendToGroupMemberIfAllowed(UUID userId, UUID groupId, String notiType,
                                             String title, String body) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        AlarmUserAgree agree = alarmUserAgreeRepository.findByUserId(userId).orElse(null);

        // 1단계: 전체 알림 마스터
        if (agree != null && !Boolean.TRUE.equals(agree.getIsAllNotiEnabled())) {
            log.info("[FCM] 전체 알림 OFF - userId: {}", userId);
            return;
        }
        // 2단계: 그룹 알림 전체 마스터
        if (agree != null && !Boolean.TRUE.equals(agree.getIsGroupNotiAllGlobalEnabled())) {
            log.info("[FCM] 그룹 알림 전체 OFF - userId: {}", userId);
            return;
        }

        // 3단계: 그룹별 마스터 + 4단계: 세부 설정
        GroupAlarmSetting setting = groupAlarmSettingRepository
                .findByUserIdAndGroupId(userId, groupId)
                .orElse(null);

        if (setting != null) {
            if (!Boolean.TRUE.equals(setting.getIsGroupNotiEnabled())) {
                log.info("[FCM] 그룹 알림 OFF - userId: {}, groupId: {}", userId, groupId);
                return;
            }
            boolean detailAllowed = switch (notiType) {
                case "newTask"      -> Boolean.TRUE.equals(setting.getIsNewTaskNotiEnabled());
                case "newMember"    -> Boolean.TRUE.equals(setting.getIsNewMemberNotiEnabled());
                case "taskComplete" -> Boolean.TRUE.equals(setting.getIsTaskCompleteNotiEnabled());
                default             -> true;
            };
            if (!detailAllowed) {
                log.info("[FCM] 세부 알림 OFF - userId: {}, type: {}", userId, notiType);
                return;
            }
        }

        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            log.warn("[FCM] FCM 토큰 없음 - userId: {}", userId);
            return;
        }

        sendMessage(user.getFcmToken(), title, body);
    }

    // FCM 메시지 실제 전송
    private void sendMessage(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("[FCM] 전송 성공 - messageId: {}", response);

        } catch (FirebaseMessagingException e) {
            log.error("[FCM] 전송 실패 - token: {}, error: {}", fcmToken, e.getMessage());
        }
    }
}
