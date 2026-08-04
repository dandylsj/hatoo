package com.hatoo.domain.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Schema(description = "그룹 내 집안일 기여도 랭킹 응답")
public class TaskRankingResponse {

    @Schema(description = "순위")
    private int rank;

    @Schema(description = "사용자 ID")
    private UUID userId;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "그룹 내 프로필 색상 코드")
    private String profileImg;

    @Schema(description = "그룹 전체 완료된 할 일 수")
    private int totalCount;

    @Schema(description = "내가 완료한 할 일 수")
    private int finishedCount;

    @Schema(description = "기여도 % (내 완료 / 그룹 전체 완료 * 100)")
    private int percent;
}
