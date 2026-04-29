package com.hatoo.domain.weeklyStats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.UUID;

@Getter
@Schema(description = "주차별 멤버 기여도 순위 항목")
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

    private WeeklyStatsResponse(int rank, UUID userId, String nickname, String profileImg,
                                int totalCount, int finishedCount, int percent) {
        this.rank = rank;
        this.userId = userId;
        this.nickname = nickname;
        this.profileImg = profileImg;
        this.totalCount = totalCount;
        this.finishedCount = finishedCount;
        this.percent = percent;
    }

    public static WeeklyStatsResponse from(WeeklyStats stats, int rank) {
        return new WeeklyStatsResponse(
                rank,
                stats.getUserId(),
                stats.getNickname(),
                stats.getProfileImg(),
                stats.getTotalCount(),
                stats.getFinishedCount(),
                stats.getPercent()
        );
    }
}
