package com.hatoo.domain.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class TaskRankingResponse {
    private UUID userId;        // 유저 ID
    private String nickname;    // 닉네임
    private Integer percent;    // 완료율
    private String profileImg;  // 그룹 내 프로필 이미지
}
