package com.hatoo.domain.alarm;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.alarm.dto.NotificationHistoryResponse;
import com.hatoo.domain.user.User;
import com.hatoo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationHistoryRepository notificationHistoryRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // 내 알림 목록 조회 (최신순)
    @Transactional(readOnly = true)
    public List<NotificationHistoryResponse> getMyNotifications(String accessToken) {
        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        return notificationHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(NotificationHistoryResponse::new)
                .collect(Collectors.toList());
    }

    // 전체 읽음 처리
    @Transactional
    public void markAllAsRead(String accessToken) {
        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        notificationHistoryRepository.markAllAsReadByUserId(user.getId());
    }

    //단건 읽음 처리
    @Transactional
    public void markRead(String accessToken, UUID notificationId) {
        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        NotificationHistory notification = notificationHistoryRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorMessage.NOTIFICATION_NOT_FOUND));

        // 소유자 검증: 내 알림이 아니면 접근 거부
        if (!notification.getUserId().equals(user.getId())) {
            throw new CustomException(ErrorMessage.ACCESS_DENIED);
        }

        notification.markAsRead();
    }
}
