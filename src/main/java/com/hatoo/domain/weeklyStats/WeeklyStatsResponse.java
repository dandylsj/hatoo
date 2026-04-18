package com.hatoo.domain.weeklyStats;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class WeeklyStatsResponse {
    private int rank;
    private UUID userId;
    private String nickname;
    private String profileImg;
    private int totalCount;
    private int finishedCount;
    private int percent;
    private String weekStart;
    private String weekEnd;

    public static WeeklyStatsResponse from(WeeklyStats stats, int rank) {
        return new WeeklyStatsResponse(
                rank,
                stats.getUserId(),
                stats.getNickname(),
                stats.getProfileImg(),
                stats.getTotalCount(),
                stats.getFinishedCount(),
                stats.getPercent(),
                stats.getWeekStart(),
                stats.getWeekEnd()
        );
    }
}
