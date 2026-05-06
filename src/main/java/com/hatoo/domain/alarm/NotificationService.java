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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
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

    // 주간 차트 N 뱃지 조회
    // 월요일 8:00 ~ 자정(화요일 00:00) 범위 안에서만 true 반환
    @Transactional(readOnly = true)
    public boolean hasUnreadWeeklyChart(String accessToken) {
        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        ZoneId kst = ZoneId.of("Asia/Seoul");
        LocalDateTime now = LocalDateTime.now(kst);

        // 가장 최근 월요일 8:00 ~ 화요일 00:00 계산
        LocalDate lastMonday = LocalDate.now(kst)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime windowStart = lastMonday.atTime(8, 0);       // 월요일 8:00
        LocalDateTime windowEnd = lastMonday.plusDays(1).atStartOfDay(); // 화요일 00:00

        // 현재 시각이 발표 시간대 밖이면 뱃지 표시 불필요
        if (now.isBefore(windowStart) || !now.isBefore(windowEnd)) {
            return false;
        }

        // 이번 주 월요일 8시 이후 생성된 읽지 않은 주간 차트 알림 확인
        return notificationHistoryRepository
                .existsByUserIdAndTypeAndIsReadFalseAndCreatedAtAfter(
                        user.getId(), AlarmType.WEEKLY_CHART, windowStart);
    }

    // 주간 차트 알림 읽음 처리
    @Transactional
    public void markWeeklyChartAsRead(String accessToken) {
        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        notificationHistoryRepository
                .markAsReadByUserIdAndType(user.getId(), AlarmType.WEEKLY_CHART);
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
