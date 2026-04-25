package com.hatoo.domain.alarm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
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
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final JwtUtil jwtUtil;

    // ──────────────────────────────────────────
    // 1. 할일 시작 알림 (스케줄러 호출)
    // ──────────────────────────────────────────
    public void sendTaskStart(UUID userId, String taskTitle) {
        String body = String.format(AlarmType.TASK_START.getBodyTemplate(), taskTitle);
        sendToUserIfAllowed(userId, AlarmType.TASK_START, body);
    }

    // ──────────────────────────────────────────
    // 2. 할일 마감 임박 알림 (스케줄러 호출)
    // ──────────────────────────────────────────
    public void sendTaskDeadline(UUID userId, String taskTitle) {
        String body = String.format(AlarmType.TASK_DEADLINE.getBodyTemplate(), taskTitle);
        sendToUserIfAllowed(userId, AlarmType.TASK_DEADLINE, body);
    }

    // ──────────────────────────────────────────
    // 3. 마감 초과 알림 (스케줄러 호출)
    // ──────────────────────────────────────────
    public void sendTaskOverdue(UUID userId, String taskTitle) {
        String body = String.format(AlarmType.TASK_OVERDUE.getBodyTemplate(), taskTitle);
        sendToUserIfAllowed(userId, AlarmType.TASK_OVERDUE, body);
    }

    // ──────────────────────────────────────────
    // 4. 주간 차트 공개 알림 (매주 월요일 8시 스케줄러 호출)
    // ──────────────────────────────────────────
    public void sendWeeklyChart(UUID groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.forEach(gm ->
                sendToGroupMemberIfAllowed(gm.getUser().getId(), groupId, AlarmType.WEEKLY_CHART,
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
                sendToGroupMemberIfAllowed(gm.getUser().getId(), groupId, AlarmType.NEW_MEMBER, body)
        );
    }

    // ──────────────────────────────────────────
    // 6. 새 집안일 등록 알림 (TaskService에서 호출)
    // ──────────────────────────────────────────
    public void sendTaskCreated(UUID groupId, String creatorNickname, String taskTitle) {
        String body = String.format(AlarmType.TASK_CREATED.getBodyTemplate(), creatorNickname, taskTitle);
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.forEach(gm ->
                sendToGroupMemberIfAllowed(gm.getUser().getId(), groupId, AlarmType.TASK_CREATED, body)
        );
    }

    // ──────────────────────────────────────────
    // 7. 집안일 배정 알림 (TaskService에서 호출) - 그룹 전체 알림
    // ──────────────────────────────────────────
    public void sendTaskAssigned(UUID groupId, String assignerNickname, String assigneeNickname) {
        String body = String.format(AlarmType.TASK_ASSIGNED.getBodyTemplate(), assignerNickname, assigneeNickname);
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.forEach(gm ->
                sendToGroupMemberIfAllowed(gm.getUser().getId(), groupId, AlarmType.TASK_ASSIGNED, body)
        );
    }

    // ──────────────────────────────────────────
    // 8. 비활성 그룹 알림 (월간 스케줄러 호출)
    // ──────────────────────────────────────────
    public void sendInactiveGroup(UUID groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.forEach(gm ->
                sendToGroupMemberIfAllowed(gm.getUser().getId(), groupId, AlarmType.INACTIVE_GROUP,
                        AlarmType.INACTIVE_GROUP.getBodyTemplate())
        );
    }

    // ──────────────────────────────────────────
    // 내부 공통 메서드
    // ──────────────────────────────────────────

    // 개인 알림: 전체 알림 마스터 → 개인 알림 토글 2단계 확인
    private void sendToUserIfAllowed(UUID userId, AlarmType type, String body) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        AlarmUserAgree agree = alarmUserAgreeRepository.findByUserId(userId).orElse(null);

        if (agree != null && Boolean.FALSE.equals(agree.getIsAllNotiEnabled())) {
            log.info("[FCM] 전체 알림 OFF - userId: {}", userId);
            return;
        }
        if (agree != null && Boolean.FALSE.equals(agree.getIsPersonalNotiEnabled())) {
            log.info("[FCM] 개인 알림 OFF - userId: {}", userId);
            return;
        }

        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            log.warn("[FCM] FCM 토큰 없음 - userId: {}", userId);
            return;
        }

        sendMessage(userId, type, user.getFcmToken(), type.getTitle(), body);
    }

    // 그룹 알림: 개인 그룹 여부에 따라 분기
    private void sendToGroupMemberIfAllowed(UUID userId, UUID groupId, AlarmType type, String body) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        AlarmUserAgree agree = alarmUserAgreeRepository.findByUserId(userId).orElse(null);

        // 1단계: 전체 알림 마스터
        if (agree != null && Boolean.FALSE.equals(agree.getIsAllNotiEnabled())) {
            log.info("[FCM] 전체 알림 OFF - userId: {}", userId);
            return;
        }

        // 개인 그룹 여부 확인
        GroupMember groupMember = groupMemberRepository.findByUserIdAndGroupId(userId, groupId).orElse(null);
        boolean isPersonalGroup = groupMember != null && groupMember.isPersonal();

        if (isPersonalGroup) {
            // 개인 그룹: 개인 알림 토글 확인
            if (agree != null && Boolean.FALSE.equals(agree.getIsPersonalNotiEnabled())) {
                log.info("[FCM] 개인 알림 OFF (개인 그룹) - userId: {}", userId);
                return;
            }
        } else {
            // 일반 그룹: 2단계 그룹 알림 전체 마스터
            if (agree != null && Boolean.FALSE.equals(agree.getIsGroupNotiAllGlobalEnabled())) {
                log.info("[FCM] 그룹 알림 전체 OFF - userId: {}", userId);
                return;
            }

            GroupAlarmSetting setting = groupAlarmSettingRepository
                    .findByUserIdAndGroupId(userId, groupId).orElse(null);

            // 3단계: 개별 그룹 알림 마스터
            if (setting != null && Boolean.FALSE.equals(setting.getIsGroupNotiEnabled())) {
                log.info("[FCM] 그룹별 알림 OFF - userId: {}, groupId: {}", userId, groupId);
                return;
            }

            // 4단계: 세부 알림 타입별 설정
            if (setting != null) {
                switch (type) {
                    case TASK_CREATED:
                        if (Boolean.FALSE.equals(setting.getIsNewTaskNotiEnabled())) {
                            log.info("[FCM] 새 집안일 알림 OFF - userId: {}", userId);
                            return;
                        }
                        break;
                    case NEW_MEMBER:
                        if (Boolean.FALSE.equals(setting.getIsNewMemberNotiEnabled())) {
                            log.info("[FCM] 새 멤버 알림 OFF - userId: {}", userId);
                            return;
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            log.warn("[FCM] FCM 토큰 없음 - userId: {}", userId);
            return;
        }

        sendMessage(userId, type, user.getFcmToken(), type.getTitle(), body);
    }

    // 실제 FCM 전송 + DB 저장 (이 메서드까지 왔으면 모든 권한 체크 통과)
    private void sendMessage(UUID userId, AlarmType type, String fcmToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setToken(fcmToken)
                    .build();
            FirebaseMessaging.getInstance().send(message);

            // 전송 성공 시에만 DB에 알림 내역 저장
            notificationHistoryRepository.save(new NotificationHistory(userId, type, title, body));
            log.info("[FCM] 알림 전송 성공 - userId: {}, type: {}", userId, type);
        } catch (FirebaseMessagingException e) {
            log.error("[FCM] 알림 전송 실패 - userId: {}, {}", userId, e.getMessage());
        }
    }
}
