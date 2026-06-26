package com.hatoo.domain.alarm;

import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final UserRepository userRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final AlarmUserAgreeRepository alarmUserAgreeRepository;
    private final GroupAlarmSettingRepository groupAlarmSettingRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final JwtUtil jwtUtil;

    // ──────────────────────────────────────────
    // 1. 할일 시작 알림 (스케줄러 호출)
    // ──────────────────────────────────────────
    @Async
    public void sendTaskStart(UUID userId, String taskTitle, UUID taskId) {
        String body = String.format(AlarmType.TASK_START.getBodyTemplate(), taskTitle);
        sendToUserIfAllowed(userId, AlarmType.TASK_START, body, taskId);
    }

    // ──────────────────────────────────────────
    // 2. 할일 마감 임박 알림 (스케줄러 호출)
    // ──────────────────────────────────────────
    @Async
    public void sendTaskDeadline(UUID userId, String taskTitle, UUID taskId) {
        String body = String.format(AlarmType.TASK_DEADLINE.getBodyTemplate(), taskTitle);
        sendToUserIfAllowed(userId, AlarmType.TASK_DEADLINE, body, taskId);
    }

    // ──────────────────────────────────────────
    // 3. 마감 초과 알림 (스케줄러 호출)
    // ──────────────────────────────────────────
    @Async
    public void sendTaskOverdue(UUID userId, String taskTitle, UUID taskId) {
        String body = String.format(AlarmType.TASK_OVERDUE.getBodyTemplate(), taskTitle);
        sendToUserIfAllowed(userId, AlarmType.TASK_OVERDUE, body, taskId);
    }

    // ──────────────────────────────────────────
    // 4. 주간 차트 공개 알림 (매주 월요일 8시 스케줄러 호출)
    //    notifiedUsers: 이미 알림을 보낸 유저 Set — 여러 그룹에 속해도 1번만 발송
    // ──────────────────────────────────────────
    public void sendWeeklyChart(UUID groupId, java.util.Set<UUID> notifiedUsers) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.forEach(gm -> {
            UUID userId = gm.getUser().getId();
            if (notifiedUsers.contains(userId)) return;
            notifiedUsers.add(userId);
            sendToGroupMemberIfAllowed(userId, groupId, AlarmType.WEEKLY_CHART,
                    AlarmType.WEEKLY_CHART.getBodyTemplate(), null);
        });
    }

    // ──────────────────────────────────────────
    // 5. 새 멤버 참여 알림 (GroupService에서 호출)
    // ──────────────────────────────────────────
    @Async
    public void sendNewMember(UUID groupId, String groupName, String newMemberNickname) {
        String body = String.format(AlarmType.NEW_MEMBER.getBodyTemplate(), groupName, newMemberNickname);
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.forEach(gm ->
                sendToGroupMemberIfAllowed(gm.getUser().getId(), groupId, AlarmType.NEW_MEMBER, body, null)
        );
    }

    // ──────────────────────────────────────────
    // 6. 새 집안일 등록 알림 (TaskService에서 호출)
    //    assigneeIds 는 배정받은 사람들 → 그룹 전체 알림에서 제외 (배정 알림이 따로 가므로)
    //    creatorId 는 할일 생성자 → 본인이 만든 할일 알림은 받지 않아야 하므로 제외
    // ──────────────────────────────────────────
    @Async
    public void sendTaskCreated(UUID groupId, String creatorNickname, String taskTitle, UUID taskId, java.util.List<UUID> assigneeIds, UUID creatorId) {
        String body = String.format(AlarmType.TASK_CREATED.getBodyTemplate(), creatorNickname, taskTitle);
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.stream()
                .filter(gm -> !assigneeIds.contains(gm.getUser().getId()))
                .filter(gm -> creatorId == null || !creatorId.equals(gm.getUser().getId()))
                .forEach(gm ->
                        sendToGroupMemberIfAllowed(gm.getUser().getId(), groupId, AlarmType.TASK_CREATED, body, taskId)
                );
    }

    // ──────────────────────────────────────────
    // 7. 집안일 배정 알림 (TaskService에서 호출) - 개인 알림
    // ──────────────────────────────────────────
    @Async
    public void sendTaskAssigned(UUID assigneeId, String assignerNickname, String assigneeNickname, UUID taskId) {
        String body = String.format(AlarmType.TASK_ASSIGNED.getBodyTemplate(), assignerNickname, assigneeNickname);
        sendToUserIfAllowed(assigneeId, AlarmType.TASK_ASSIGNED, body, taskId);
    }

    // ──────────────────────────────────────────
    // 8. 비활성 그룹 알림 (월간 스케줄러 호출)
    // ──────────────────────────────────────────
    @Async
    public void sendInactiveGroup(UUID groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.forEach(gm ->
                sendToGroupMemberIfAllowed(gm.getUser().getId(), groupId, AlarmType.INACTIVE_GROUP,
                        AlarmType.INACTIVE_GROUP.getBodyTemplate(), null)
        );
    }

    // ──────────────────────────────────────────
    // 9. 강제 탈퇴 알림 (GroupService에서 호출)
    // ──────────────────────────────────────────
    @Async
    public void sendForcedLeave(UUID userId, String groupName) {
        String body = String.format(AlarmType.FORCED_LEAVE.getBodyTemplate(), groupName);
        sendToUserIfAllowed(userId, AlarmType.FORCED_LEAVE, body, null);
    }

    // ──────────────────────────────────────────
    // 내부 공통 메서드
    // ──────────────────────────────────────────

    // 개인 알림: 전체 알림 마스터 → 개인 알림 토글 2단계 확인
    private void sendToUserIfAllowed(UUID userId, AlarmType type, String body, UUID taskId) {
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

        List<UserFcmToken> tokens = userFcmTokenRepository.findByUser(user);
        if (tokens.isEmpty()) {
            log.warn("[FCM] FCM 토큰 없음 - userId: {}", userId);
            return;
        }
        notificationHistoryRepository.save(new NotificationHistory(userId, type, type.getTitle(), body, taskId));
        sendMulticast(userId, type, tokens.stream().map(UserFcmToken::getFcmToken).toList(),
                type.getTitle(), body, taskId);
    }

    // 그룹 알림: 개인 그룹 여부에 따라 분기
    private void sendToGroupMemberIfAllowed(UUID userId, UUID groupId, AlarmType type, String body, UUID taskId) {
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

        List<UserFcmToken> tokens = userFcmTokenRepository.findByUser(user);
        if (tokens.isEmpty()) {
            log.warn("[FCM] FCM 토큰 없음 - userId: {}", userId);
            return;
        }
        notificationHistoryRepository.save(new NotificationHistory(userId, type, type.getTitle(), body, taskId));
        sendMulticast(userId, type, tokens.stream().map(UserFcmToken::getFcmToken).toList(),
                type.getTitle(), body, taskId);
    }

    // 멀티캐스트 FCM 전송 - 한 유저의 여러 토큰을 Firebase API 1회로 처리
    private void sendMulticast(UUID userId, AlarmType type, List<String> fcmTokens, String title, String body, UUID taskId) {
        if (fcmTokens.isEmpty()) return;
        try {
            MulticastMessage.Builder builder = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder().setSound("default").build())
                            .build())
                    .addAllTokens(fcmTokens);

            if (taskId != null) {
                builder.putData("taskId", taskId.toString());
            }

            BatchResponse batchResponse = FirebaseMessaging.getInstance().sendEachForMulticast(builder.build());

            List<SendResponse> responses = batchResponse.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                SendResponse sr = responses.get(i);
                if (!sr.isSuccessful()) {
                    MessagingErrorCode errorCode = sr.getException().getMessagingErrorCode();
                    if (errorCode == MessagingErrorCode.UNREGISTERED
                            || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                        log.warn("[FCM] 무효 토큰 삭제 - userId: {}, errorCode: {}", userId, errorCode);
                        userFcmTokenRepository.deleteByFcmToken(fcmTokens.get(i));
                    } else {
                        log.error("[FCM] 전송 실패 - userId: {}, errorCode: {}", userId, errorCode);
                    }
                }
            }

            log.info("[FCM] 전송 완료 - userId: {}, type: {}, 성공: {}/{}",
                    userId, type, batchResponse.getSuccessCount(), fcmTokens.size());

        } catch (FirebaseMessagingException e) {
            log.error("[FCM] 멀티캐스트 오류 - userId: {}, message: {}", userId, e.getMessage());
        }
    }
}