package com.hatoo.domain.task;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

// Task.dueFrom / dueTo 문자열 파싱·포맷을 한 곳에서 관리 (알림 스케줄러와 반복 할일 생성기가 함께 사용)
@Slf4j
public final class DueDateTimeParser {

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter STORAGE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private DueDateTimeParser() {}

    public static LocalDateTime parse(String due) {
        if (due == null || due.isBlank()) return null;

        // 1. ISO 8601 형식 "2026-04-27T01:56:04.689Z" (UTC → KST 변환)
        if (due.contains("T")) {
            try {
                Instant instant = Instant.parse(due);
                return LocalDateTime.ofInstant(instant, KST);
            } catch (Exception ignored) {}
            // Z 없는 ISO 형식 "2026-04-27T13:55:01"
            try {
                return LocalDateTime.parse(due, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception ignored) {}
        }

        // 2. 공백 구분 형식 "2026-04-28 13:55:01.884884" 등
        String[] patterns = {
            "yyyy-MM-dd HH:mm:ss.SSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm"
        };

        for (String pattern : patterns) {
            try {
                String trimmed = due.length() > pattern.length() ? due.substring(0, pattern.length()) : due;
                return LocalDateTime.parse(trimmed, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {}
        }

        // 3. 날짜만 "2026-04-28" → 자정 00:00
        try {
            return LocalDate.parse(due.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        } catch (Exception ignored) {}

        log.warn("[DueDateTimeParser] 날짜 파싱 실패 - due: {}", due);
        return null;
    }

    // 반복 할일 재생성 시 저장할 문자열로 변환 (시간 정보를 반드시 포함해 다음 회차도 원래 시각을 유지)
    public static String format(LocalDateTime dateTime) {
        return dateTime.format(STORAGE_FORMAT);
    }
}
