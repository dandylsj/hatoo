package com.hatoo.domain.activitylog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final UserSignupLogRepository userSignupLogRepository;
    private final TaskCreationLogRepository taskCreationLogRepository;

    @Transactional
    public void recordSignup(UUID userId, String email, String nickname, SignupProvider provider) {
        userSignupLogRepository.save(new UserSignupLog(userId, email, nickname, provider));
        log.info("[ActivityLog] 유저 가입 - userId: {}, provider: {}", userId, provider);
    }

    @Transactional
    public void recordTaskCreation(UUID taskId, UUID creatorId, String creatorNickname, UUID groupId, String title) {
        taskCreationLogRepository.save(new TaskCreationLog(taskId, creatorId, creatorNickname, groupId, title));
        log.info("[ActivityLog] 할일 등록 - taskId: {}, creatorId: {}", taskId, creatorId);
    }
}
