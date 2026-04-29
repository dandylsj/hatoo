package com.hatoo.domain.weeklyStats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Schema(description = "주차별 통계 조회 응답 (데이터 없을 때도 주차 정보 포함)")
public class WeeklyStatsWrapperResponse {

    @Schema(description = "주차 시작일 (월요일)", example = "2026-04-13")
    private String weekStart;

    @Schema(description = "주차 종료일 (일요일)", example = "2026-04-19")
    private String weekEnd;

    @Schema(description = "몇 월 몇 주차", example = "4월 2주차")
    private String weekLabel;

    @Schema(description = "날짜 범위 표시", example = "4월 13일 ~ 4월 19일")
    private String weekRange;

    @Schema(description = "멤버별 기여도 순위 (데이터 없으면 빈 배열)")
    private List<WeeklyStatsResponse> rankings;

    private WeeklyStatsWrapperResponse(String weekStart, String weekEnd,
                                       String weekLabel, String weekRange,
                                       List<WeeklyStatsResponse> rankings) {
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.weekLabel = weekLabel;
        this.weekRange = weekRange;
        this.rankings = rankings;
    }

    public static WeeklyStatsWrapperResponse of(LocalDate start, LocalDate end,
                                                List<WeeklyStatsResponse> rankings) {
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("M월 d일");
        DateTimeFormatter isoFmt = DateTimeFormatter.ISO_LOCAL_DATE;

        int month = start.getMonthValue();
        int weekOfMonth = (start.getDayOfMonth() - 1) / 7 + 1;
        String weekLabel = month + "월 " + weekOfMonth + "주차";
        String weekRange = start.format(labelFmt) + " ~ " + end.format(labelFmt);

        return new WeeklyStatsWrapperResponse(
                start.format(isoFmt),
                end.format(isoFmt),
                weekLabel,
                weekRange,
                rankings
        );
    }
}
