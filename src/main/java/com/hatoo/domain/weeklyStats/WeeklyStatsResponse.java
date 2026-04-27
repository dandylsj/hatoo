package com.hatoo.domain.weeklyStats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@Schema(description = "주차별 집안일 통계 응답")
public class WeeklyStatsResponse {

    @Schema(description = "순위", example = "1")
    private int rank;

    @Schema(description = "유저 ID")
    private UUID userId;

    @Schema(description = "닉네임", example = "고맙미")
    private String nickname;

    @Schema(description = "그룹 내 프로필 이미지 색상", example = "YELLOW")
    private String profileImg;

    @Schema(description = "그룹 전체 완료된 할일 수", example = "20")
    private int totalCount;

    @Schema(description = "내가 완료한 할일 수", example = "18")
    private int finishedCount;

    @Schema(description = "기여도 (내 완료 / 그룹 전체 완료 * 100)", example = "90")
    private int percent;

    @Schema(description = "주차 시작일 (월요일)", example = "2026-08-10")
    private String weekStart;

    @Schema(description = "주차 종료일 (일요일)", example = "2026-08-16")
    private String weekEnd;

    @Schema(description = "몇 월 몇 주차", example = "8월 2주차")
    private String weekLabel;

    @Schema(description = "날짜 범위 표시", example = "8월 10일 ~ 8월 16일")
    private String weekRange;

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

        // "N월 N주차" 라벨 계산
        int month = start.getMonthValue();
        int weekOfMonth = (start.getDayOfMonth() - 1) / 7 + 1;
        String weekLabel = month + "월 " + weekOfMonth + "주차";

        // "N월 N일 ~ N월 N일" 범위 표시
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("M월 d일");
        String weekRange = start.format(labelFmt) + " ~ " + end.format(labelFmt);

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
                weekLabel,
                weekRange
        );
    }
}
