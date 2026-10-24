package com.hatoo.domain.task;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 할일의 dueFrom/dueTo 문자열로부터 알림 발송 시각(KST, 분 단위)을 계산한다.
 *
 * <p>dueFrom/dueTo는 클라이언트가 보낸 문자열을 그대로 저장하는 컬럼이라 형식이 여러 가지다
 * (UTC ISO "2026-04-27T01:56:04.689Z", 로컬 ISO, 공백 구분, 날짜만 등). 예전에는 AlarmScheduler가 매 분마다
 * 미완료 할일을 전부 불러와 이 문자열을 자바에서 파싱해 "지금 보낼 알림인지"를 판단했는데, 그러면 DB가 시각으로
 * 거를 수가 없어서 반복 할일이 쌓일수록 매 분 읽는 양이 계속 늘어났다. 그래서 할일을 저장/수정하는 시점에
 * 한 번만 파싱해서 알림 시각을 DATETIME 컬럼에 저장해두고, 스케줄러는 인덱스로 "지금 이 1분"만 조회한다.
 */
@Slf4j
public final class TaskAlarmTimes {

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");
    /** 마감 초과 알림은 마감 2시간 뒤에 보낸다. */
    static final Duration OVERDUE_DELAY = Duration.ofHours(2);

    private static final String[] PATTERNS = {
            "yyyy-MM-dd HH:mm:ss.SSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm"
    };

    private TaskAlarmTimes() {
    }

    /** 시작 알림 시각 = dueFrom (분 단위 절삭). */
    static LocalDateTime startAlarmAt(String dueFrom) {
        LocalDateTime dueFromAt = parse(dueFrom);
        return dueFromAt == null ? null : dueFromAt.truncatedTo(ChronoUnit.MINUTES);
    }

    /** 마감 임박 알림 시각 = dueTo - deadLine (마감 설정이 없으면 null). */
    static LocalDateTime deadlineAlarmAt(String dueTo, DeadLine deadLine) {
        LocalDateTime dueToAt = parse(dueTo);
        Duration duration = toDuration(deadLine);
        if (dueToAt == null || duration == null) return null;
        return dueToAt.minus(duration).truncatedTo(ChronoUnit.MINUTES);
    }

    /** 마감 초과 알림 시각 = dueTo + 2시간. */
    static LocalDateTime overdueAlarmAt(String dueTo) {
        LocalDateTime dueToAt = parse(dueTo);
        return dueToAt == null ? null : dueToAt.plus(OVERDUE_DELAY).truncatedTo(ChronoUnit.MINUTES);
    }

    public static LocalDateTime parse(String due) {
        if (due == null || due.isBlank()) return null;

        // 1. ISO 8601 형식 "2026-04-27T01:56:04.689Z" (UTC → KST 변환)
        if (due.contains("T")) {
            try {
                return LocalDateTime.ofInstant(Instant.parse(due), KST);
            } catch (Exception ignored) {}
            // Z 없는 ISO 형식 "2026-04-27T13:55:01"
            try {
                return LocalDateTime.parse(due, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception ignored) {}
        }

        // 2. 공백 구분 형식 "2026-04-28 13:55:01.884884" 등
        for (String pattern : PATTERNS) {
            try {
                String trimmed = due.length() > pattern.length() ? due.substring(0, pattern.length()) : due;
                return LocalDateTime.parse(trimmed, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {}
        }

        // 3. 날짜만 "2026-04-28" → 자정 00:00
        try {
            return LocalDate.parse(due.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        } catch (Exception ignored) {}

        log.warn("[TaskAlarmTimes] 날짜 파싱 실패 - due: {}", due);
        return null;
    }

    private static Duration toDuration(DeadLine deadLine) {
        if (deadLine == null) return null;
        return switch (deadLine) {
            case MIN_10 -> Duration.ofMinutes(10);
            case MIN_30 -> Duration.ofMinutes(30);
            case HOUR_1 -> Duration.ofHours(1);
            case DAY_1 -> Duration.ofDays(1);
            case WEEK_1 -> Duration.ofDays(7);
            case NONE -> null;
        };
    }
}
