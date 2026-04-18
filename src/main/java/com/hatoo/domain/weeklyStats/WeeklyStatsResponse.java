package com.hatoo.domain.weeklyStats;

import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
public class WeeklyStatsResponse {
    private int rank;
    private UUID userId;
    private String nickname;
    private String profileImg;
    private int totalCount;
    private int finishedCount;
    private int percent;
    private String weekStart;   // "2026-08-10"
    private String weekEnd;     // "2026-08-16"
    private String weekLabel;   // "8월 2주차"
    private String weekRange;   // "8월 10일 ~ 8월 16일"

    private WeeklyStatsResponse(int rank, UUID userId, String nickname, String profileImg,
                                int totalCount, int finishedCount, int percent,
                                String weekStart, String weekEnd,
                                String weekLabel, String weekRange) {
        this.rank = rank;
        this.userId = userId;
        this.nickname = nickname;
        this.profileImg = profileImg;
        this.totalCount = totalCount;
        this.finishedCount = finishedCount;
        this.percent = percent;
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.weekLabel = weekLabel;
        this.weekRange = weekRange;
    }

    public static WeeklyStatsResponse from(WeeklyStats stats, int rank) {
        LocalDate start = LocalDate.parse(stats.getWeekStart());
        LocalDate end = LocalDate.parse(stats.getWeekEnd());

        return new WeeklyStatsResponse(
                rank,
                stats.getUserId(),
                stats.getNickname(),
                stats.getProfileImg(),
                stats.getTotalCount(),
                stats.getFinishedCount(),
                stats.getPercent(),
                stats.getWeekStart(),
                stats.getWeekEnd(),
                buildWeekLabel(start),
                buildWeekRange(start, end)
        );
    }

    // "8월 2주차" 형태로 변환
    // weekStart(월요일) 기준으로 해당 월의 몇 번째 주인지 계산
    private static String buildWeekLabel(LocalDate weekStart) {
        int month = weekStart.getMonthValue();
        int weekOfMonth = (weekStart.getDayOfMonth() - 1) / 7 + 1;
        return month + "월 " + weekOfMonth + "주차";
    }

    // "8월 10일 ~ 8월 16일" 형태로 변환
    private static String buildWeekRange(LocalDate start, LocalDate end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일");
        return start.format(formatter) + " ~ " + end.format(formatter);
    }
}
