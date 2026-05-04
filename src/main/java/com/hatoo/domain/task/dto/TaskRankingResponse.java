package com.hatoo.domain.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class TaskRankingResponse {
    private int rank;            // 순위
    private UUID userId;         // 유저 ID
    private String nickname;     // 닉네임
    private String profileImg;   // 그룹 내 프로필 이미지
    private int totalCount;      // 그룹 전체 완료된 할일 수
    private int finishedCount;   // 내가 완료한 할일 수
    private int percent;         // 기여도 (내 완료 / 그룹 전체 완료 * 100)
}
